package com.petsave.petsave.Service;

import com.petsave.petsave.Entity.Donation;
import com.petsave.petsave.Entity.PaymentStatus;
import com.petsave.petsave.Entity.User;
import com.petsave.petsave.Repository.DonationRepository;
import com.petsave.petsave.dto.DonationRequest;
import com.petsave.petsave.dto.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class NewDonationService {

    @Value("${paystack.secret.key}")
    private String paystackSecretKey;

    private final DonationRepository donationRepository;
    private final WebClient.Builder webClientBuilder;

    private static final String PAYSTACK_INIT_URL = "https://api.paystack.co/transaction/initialize";

    public NewDonationService(DonationRepository donationRepository, WebClient.Builder webClientBuilder) {
        this.donationRepository = donationRepository;
        this.webClientBuilder = webClientBuilder;
    }

    public PaymentResponse initializePayment(DonationRequest donationRequest, Authentication auth) {
        try {
            log.info("Starting payment initialization for: {}", donationRequest.getEmail());
            
            // Generate unique reference
            String reference = "PET_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            
            // Create donation record
            Donation donation = createDonationRecord(donationRequest, reference, auth);
            donationRepository.save(donation);
            
            log.info("Donation record created with reference: {}", reference);
            
            // Call Paystack API
            Map<String, Object> paystackResponse = callPaystackAPI(donation, reference);
            
            if (paystackResponse != null && (Boolean) paystackResponse.get("status")) {
                Map<String, Object> data = (Map<String, Object>) paystackResponse.get("data");
                String authorizationUrl = (String) data.get("authorization_url");
                String accessCode = (String) data.get("access_code");
                
                log.info("Paystack payment initialized successfully for reference: {}", reference);
                return PaymentResponse.success(authorizationUrl, reference, accessCode);
            } else {
                String errorMsg = paystackResponse != null ? 
                    (String) paystackResponse.get("message") : "Paystack API call failed";
                log.error("Paystack API returned error: {}", errorMsg);
                return PaymentResponse.error("Payment initialization failed: " + errorMsg);
            }
            
        } catch (Exception e) {
            log.error("Error in payment initialization: {}", e.getMessage(), e);
            return PaymentResponse.error("Payment initialization failed: " + e.getMessage());
        }
    }

    private Donation createDonationRecord(DonationRequest donationRequest, String reference, Authentication auth) {
        Donation donation = new Donation();
        donation.setDonorName(donationRequest.getDonorName());
        donation.setEmail(donationRequest.getEmail());
        donation.setAmount(donationRequest.getAmount());
        donation.setGender(donationRequest.getGender());
        donation.setCountry(donationRequest.getCountry());
        donation.setDate(LocalDateTime.now());
        donation.setReference(reference);
        donation.setPaymentStatus(PaymentStatus.PENDING);

        // Set user if authenticated
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            User user = (User) auth.getPrincipal();
            donation.setUser(user);
            donation.setEmail(user.getEmail());
        }

        return donation;
    }

    private Map<String, Object> callPaystackAPI(Donation donation, String reference) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("email", donation.getEmail());
            payload.put("amount", (int) (donation.getAmount() * 100)); // Convert to kobo
            payload.put("reference", reference);
            payload.put("callback_url", "http://localhost:3000/payment/callback?ref=" + reference);

            log.info("Calling Paystack API with payload: {}", payload);

            WebClient webClient = webClientBuilder.build();
            Map<String, Object> response = webClient.post()
                    .uri(PAYSTACK_INIT_URL)
                    .header("Authorization", "Bearer " + paystackSecretKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            log.info("Paystack API response: {}", response);
            return response;

        } catch (Exception e) {
            log.error("Paystack API call failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call Paystack API: " + e.getMessage());
        }
    }
}
