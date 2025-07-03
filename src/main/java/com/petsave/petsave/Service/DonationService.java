package com.petsave.petsave.Service;

import com.petsave.petsave.Entity.Donation;
import com.petsave.petsave.Entity.PaymentStatus;
import com.petsave.petsave.Repository.DonationRepository;
import com.petsave.petsave.dto.DonationRequest;
import com.petsave.petsave.dto.DonationResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class DonationService {

    @Value("${paystack.secret.key}")
    private String PAYSTACK_SECRET;
    
    private String INIT_URL = "https://api.paystack.co/transaction/initialize";
    private WebClient webClient = WebClient.create();

    private final DonationRepository donationRepository;

    public DonationService(DonationRepository donationRepository) {
        this.donationRepository = donationRepository;
    }

    public Page<DonationResponse> getAllDonations(String donorName, Pageable pageable) {
        Page<Donation> donations;

        if (donorName != null && !donorName.isEmpty()) {
            donations = donationRepository.findByDonorNameContainingIgnoreCase(donorName, pageable);
        } else {
            donations = donationRepository.findAll(pageable);
        }

        return donations.map(this::mapToResponse);
    }

    public DonationResponse createDonation(DonationRequest request) {
        Donation donation = new Donation();
        donation.setDonorName(request.getDonorName());
        donation.setDate(request.getDate());
        donation.setAmount(request.getAmount());
        donation.setGender(request.getGender());
        donation.setCountry(request.getCountry());

        Donation saved = donationRepository.save(donation);
        return mapToResponse(saved);
    }

    public DonationResponse getDonationById(Long id) {
        Optional<Donation> donation = donationRepository.findById(id);
        return donation.map(this::mapToResponse).orElse(null);
    }

    public DonationResponse updateDonation(Long id, DonationRequest request) {
        Optional<Donation> optional = donationRepository.findById(id);
        if (optional.isPresent()) {
            Donation donation = optional.get();
            donation.setDonorName(request.getDonorName());
            donation.setDate(request.getDate());
            donation.setAmount(request.getAmount());
            donation.setGender(request.getGender());
            donation.setCountry(request.getCountry());

            Donation updated = donationRepository.save(donation);
            return mapToResponse(updated);
        }
        return null;
    }

    public boolean deleteDonation(Long id) {
        if (donationRepository.existsById(id)) {
            donationRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Double getTotalDonations() {
        return donationRepository.findAll()
                .stream()
                .mapToDouble(Donation::getAmount)
                .sum();
    }

    public Long getDonationCount() {
        return donationRepository.count();
    }

    private DonationResponse mapToResponse(Donation donation) {
        DonationResponse response = new DonationResponse();
        response.setId(donation.getId());
        response.setDonorName(donation.getDonorName());
        response.setAmount(donation.getAmount());
        response.setDate(donation.getDate());
        response.setGender(donation.getGender());
        response.setCountry(donation.getCountry());
        response.setPaymentStatus(donation.getPaymentStatus());

        return response;
    }


    public String initializePayment(DonationRequest donationRequest) {
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
        

        donationRepository.save(donation);

        Map<String, Object> payload = new HashMap<>();
        payload.put("email", donation.getEmail());
        payload.put("amount", (int) (donation.getAmount() * 100));
        payload.put("reference", reference);

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
