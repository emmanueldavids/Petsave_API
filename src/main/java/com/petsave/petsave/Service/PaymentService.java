package com.petsave.petsave.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
// import org.springframework.web.reactive.function.client.WebClient;

import com.petsave.petsave.Entity.Donation;
import com.petsave.petsave.Entity.PaymentStatus;
import com.petsave.petsave.Entity.User;
import com.petsave.petsave.Repository.DonationRepository;
import com.petsave.petsave.Repository.UserRepository;
import com.petsave.petsave.dto.DonationRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
 

@Service
public class PaymentService {

    @Value("${paystack.secret.key}")
    private String PAYSTACK_SECRET;

    private final DonationRepository donationRepository;
    private final WebClient webClient;
    private final UserRepository userRepository;

    private static final String INIT_URL = "https://api.paystack.co/transaction/initialize";

    public PaymentService(DonationRepository donationRepository, UserRepository userRepository) {
        this.donationRepository = donationRepository;
        this.webClient = WebClient.create(); // you can also inject this if preferred
        this.userRepository = userRepository;
    }

    public String initializePayments(DonationRequest donationRequest) {
        String reference = UUID.randomUUID().toString();

        Donation donation = new Donation();
        donation.setDonorName(donationRequest.getDonorName());
        donation.setEmail(donationRequest.getEmail());
        donation.setAmount(donationRequest.getAmount());
        donation.setGender(donationRequest.getGender());
        donation.setCountry(donationRequest.getCountry());
        donation.setDate(java.time.LocalDateTime.now());
        donation.setPaymentStatus(PaymentStatus.PENDING);
        donation.setReference(reference);

        donationRepository.save(donation); // ✅ Now works because it's injected

        Map<String, Object> payload = new HashMap<>();
        payload.put("email", donation.getEmail());
        payload.put("amount", (int) (donation.getAmount() * 100));
        payload.put("reference", reference);
        payload.put("callback_url", "https://yourfrontend.com/payment/callback?ref=" + reference);

        return webClient.post()
            .uri(INIT_URL)
            .header("Authorization", "Bearer " + PAYSTACK_SECRET)
            .header("Content-Type", "application/json")
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(Map.class)
            .map(response -> (String) ((Map<String, Object>) response.get("data")).get("authorization_url"))
            .block();
    }
}
