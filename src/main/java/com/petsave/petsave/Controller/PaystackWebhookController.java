package com.petsave.petsave.Controller;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petsave.petsave.Entity.PaymentStatus;
import com.petsave.petsave.Repository.DonationRepository;


@RestController
@RequestMapping("/api/paystack")
public class PaystackWebhookController {

    @Value("${paystack.secret.key}")
    private String PAYSTACK_SECRET;

    @Autowired
    private DonationRepository donationRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody String rawBody,
                                           @RequestHeader("x-paystack-signature") String signature) {
        try {
            // Compute HMAC SHA512 of rawBody using Paystack secret
            String computedSignature = computeHmacSHA512(rawBody, PAYSTACK_SECRET);

            if (!computedSignature.equals(signature)) {
                return ResponseEntity.status(403).body("Invalid signature");
            }

            // Parse JSON body to Map
            Map<String, Object> payload = objectMapper.readValue(rawBody, Map.class);

            String event = (String) payload.get("event");

            if ("charge.success".equals(event) || "charge.failed".equals(event)) {
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                String reference = (String) data.get("reference");

                donationRepository.findByReference(reference).ifPresent(donation -> {
                    donation.setPaymentStatus("charge.success".equals(event)
                            ? PaymentStatus.COMPLETED
                            : PaymentStatus.FAILED);
                    donationRepository.save(donation);
                });
            }

            return ResponseEntity.ok("Webhook handled");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Webhook processing error");
        }
    }

    private String computeHmacSHA512(String data, String secret) throws Exception {
        Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        sha512_HMAC.init(secretKey);

        byte[] hash = sha512_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();

        for (byte b : hash) {
            result.append(String.format("%02x", b));
        }

        return result.toString();
    }
}
