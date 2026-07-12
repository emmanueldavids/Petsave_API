package com.petsave.petsave.Service;

import com.petsave.petsave.Entity.PetSitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles Paystack Transfer Recipient creation and payouts to sitters.
 * Runs against whatever Paystack secret key is configured — this project currently
 * uses a Paystack TEST key, where transfers complete immediately without OTP.
 */
@Service
@Slf4j
public class PaystackTransferService {

    @Value("${paystack.secret.key}")
    private String paystackSecretKey;

    private static final String RECIPIENT_URL = "https://api.paystack.co/transferrecipient";
    private static final String TRANSFER_URL = "https://api.paystack.co/transfer";
    private static final String BANK_LIST_URL = "https://api.paystack.co/bank";

    private final WebClient.Builder webClientBuilder;

    public PaystackTransferService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public List<Map<String, Object>> listBanks(String currency) {
        try {
            Map<String, Object> response = webClientBuilder.build()
                    .get()
                    .uri(BANK_LIST_URL + "?currency=" + currency)
                    .header("Authorization", "Bearer " + paystackSecretKey)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (response != null && Boolean.TRUE.equals(response.get("status"))) {
                return (List<Map<String, Object>>) response.get("data");
            }
            log.error("Paystack bank list call returned non-success: {}", response);
            return List.of();
        } catch (Exception e) {
            log.error("Failed to fetch Paystack bank list: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Creates (or reuses) a Paystack transfer recipient for this sitter's bank details.
     * Returns the recipient_code, or null if it couldn't be created (e.g. missing/invalid bank details).
     */
    public String ensureRecipient(PetSitter sitter) {
        if (sitter.getPaystackRecipientCode() != null && !sitter.getPaystackRecipientCode().isBlank()) {
            return sitter.getPaystackRecipientCode();
        }
        if (sitter.getBankCode() == null || sitter.getAccountNumber() == null) {
            log.warn("Cannot create Paystack recipient for sitter {} — no bank details on file", sitter.getId());
            return null;
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "nuban");
            payload.put("name", sitter.getAccountName() != null ? sitter.getAccountName() : sitter.getUser().getName());
            payload.put("account_number", sitter.getAccountNumber());
            payload.put("bank_code", sitter.getBankCode());
            payload.put("currency", "NGN");

            Map<String, Object> response = webClientBuilder.build()
                    .post()
                    .uri(RECIPIENT_URL)
                    .header("Authorization", "Bearer " + paystackSecretKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && Boolean.TRUE.equals(response.get("status"))) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                return (String) data.get("recipient_code");
            }
            log.error("Paystack recipient creation failed for sitter {}: {}", sitter.getId(), response);
            return null;
        } catch (Exception e) {
            log.error("Paystack recipient creation error for sitter {}: {}", sitter.getId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Initiates a transfer to a recipient. Returns true only if Paystack confirmed success —
     * never claim a payout happened unless this returns true.
     */
    public boolean transfer(String recipientCode, BigDecimal amount, String reason) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("source", "balance");
            payload.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue());
            payload.put("recipient", recipientCode);
            payload.put("reason", reason);

            Map<String, Object> response = webClientBuilder.build()
                    .post()
                    .uri(TRANSFER_URL)
                    .header("Authorization", "Bearer " + paystackSecretKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && Boolean.TRUE.equals(response.get("status"))) {
                log.info("Paystack transfer succeeded for recipient {}: {}", recipientCode, response.get("data"));
                return true;
            }
            log.error("Paystack transfer failed for recipient {}: {}", recipientCode, response);
            return false;
        } catch (Exception e) {
            log.error("Paystack transfer error for recipient {}: {}", recipientCode, e.getMessage(), e);
            return false;
        }
    }
}
