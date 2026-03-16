package com.petsave.petsave.Service;

import com.petsave.petsave.Entity.Donation;
import com.petsave.petsave.Entity.PaymentStatus;
import com.petsave.petsave.Entity.User;
import com.petsave.petsave.Repository.DonationRepository;
import com.petsave.petsave.Repository.UserRepository;
import com.petsave.petsave.dto.DonationRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Service
@Slf4j
public class PaymentService {

    @Value("${paystack.secret.key}")
    private String PAYSTACK_SECRET;

    private final DonationRepository donationRepository;
    private final WebClient webClient;
    private final UserRepository userRepository;

    private static final String INIT_URL = "https://api.paystack.co/transaction/initialize";

    public PaymentService(DonationRepository donationRepository, UserRepository userRepository, WebClient.Builder webClientBuilder) {
        this.donationRepository = donationRepository;
        this.webClient = webClientBuilder.build(); // Use injected WebClient.Builder
        this.userRepository = userRepository;
    }

    public String initializePayments(DonationRequest donationRequest) {
        try {
            log.info("Initializing payment for donation: {}", donationRequest);
            
            String reference = UUID.randomUUID().toString();

            Donation donation = new Donation();
            donation.setDonorName(donationRequest.getDonorName());
            donation.setEmail(donationRequest.getEmail());
            donation.setAmount(donationRequest.getAmount());
            donation.setGender(donationRequest.getGender());
            donation.setCountry(donationRequest.getCountry());
            donation.setDate(java.time.LocalDateTime.now());
            donation.setReference(reference);
            donation.setPaymentStatus(PaymentStatus.PENDING);

            donationRepository.save(donation);

            Map<String, Object> payload = new HashMap<>();
            payload.put("email", donation.getEmail());
            payload.put("amount", (int) (donation.getAmount() * 100));
            payload.put("reference", reference);
            payload.put("callback_url", "https://yourfrontend.com/payment/callback?ref=" + reference);

            log.info("Sending Paystack request with payload: {}", payload);

            String response = webClient.post()
                    .uri(INIT_URL)
                    .header("Authorization", "Bearer " + PAYSTACK_SECRET)
                    .header("Content-Type", "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(resp -> {
                        log.info("Paystack response: {}", resp);
                        return (String) ((Map<String, Object>) resp.get("data")).get("authorization_url");
                    })
                    .block();

            log.info("Payment initialized successfully with reference: {}", reference);
            return response;
        } catch (Exception e) {
            log.error("Error initializing payment: {}", e.getMessage(), e);
            return "{\"message\":\"An unexpected error occurred. Please try again.\",\"status\":\"error\"}";
        }
    }
}
