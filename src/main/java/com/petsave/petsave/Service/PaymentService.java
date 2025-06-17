package com.petsave.petsave.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
// import org.springframework.web.reactive.function.client.WebClient;

import com.petsave.petsave.dto.DonationRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
 

@Service
public class PaymentService {

    @Value("${paystack.secret.key}")
    private String PAYSTACK_SECRET;
    @Value("${paystack.secret.key}")
    private String paystackSecret;

    private String INIT_URL = "https://api.paystack.co/transaction/initialize";
    private WebClient webClient = WebClient.create();

    public String initializePayment(DonationRequest donationRequest) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", donationRequest.getEmail());
        payload.put("amount", (int)(donationRequest.getAmount() * 100)); // Paystack expects amount in kobo
        payload.put("reference", UUID.randomUUID().toString());

        return webClient.post()
            .uri(INIT_URL)
            .header("Authorization", "Bearer " + PAYSTACK_SECRET)
            .header("Content-Type", "application/json")
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(Map.class)
            .map(response -> (String)((Map<String, Object>)response.get("data")).get("authorization_url"))
            .block(); // get the redirect URL to send to frontend
    }
}
