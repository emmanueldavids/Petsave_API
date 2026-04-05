
package com.petsave.petsave.Service;

import com.petsave.petsave.Entity.Donation;
import com.petsave.petsave.Entity.PaymentStatus;
import com.petsave.petsave.Entity.Pet;
import com.petsave.petsave.Entity.PetStatus;
import com.petsave.petsave.Entity.User;
import com.petsave.petsave.Repository.DonationRepository;
import com.petsave.petsave.Repository.PetRepository;
import com.petsave.petsave.dto.DonationRequest;
import com.petsave.petsave.dto.DonationResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
@Slf4j
public class DonationService {

    @Value("${paystack.secret.key}")
    private String PAYSTACK_SECRET;
    
    private String INIT_URL = "https://api.paystack.co/transaction/initialize";
    private WebClient webClient;

    private final DonationRepository donationRepository;
    private final PetRepository petRepository;

    public DonationService(DonationRepository donationRepository, PetRepository petRepository, WebClient.Builder webClientBuilder) {
        this.donationRepository = donationRepository;
        this.petRepository = petRepository;
        this.webClient = webClientBuilder.build();
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
        // Get total of all donations regardless of payment status
        // Since we're using mock payment for now, all donations should be counted
        return donationRepository.getTotalAmountAll();
    }

    public Long getDonationCount() {
        return donationRepository.count();
    }

    private DonationResponse mapToResponse(Donation donation) {
        DonationResponse response = new DonationResponse();
        response.setId(donation.getId());
        response.setDonorName(donation.getDonorName());
        response.setEmail(donation.getEmail());
        response.setAmount(donation.getAmount());
        response.setDate(donation.getDate());
        response.setGender(donation.getGender());
        response.setCountry(donation.getCountry());
        response.setPaymentStatus(donation.getPaymentStatus());
        response.setReference(donation.getReference());

        // ✅ Include user if not null
        if (donation.getUser() != null) {
            var user = donation.getUser();
            var userDto = new DonationResponse.UserDto();
            userDto.setId(user.getId());
            userDto.setName(user.getName());
            userDto.setEmail(user.getEmail());
            response.setUser(userDto);
        }

        return response;
    }



    public String initializePayment(DonationRequest donationRequest, Authentication auth) {
        try {
            log.info("Initializing payment for donation: {}", donationRequest);
            
            String reference = UUID.randomUUID().toString();

            Donation donation = new Donation();
            donation.setDonorName(donationRequest.getDonorName());
            donation.setEmail(donationRequest.getEmail());
            donation.setAmount(donationRequest.getAmount());
            donation.setGender(donationRequest.getGender());
            donation.setCountry(donationRequest.getCountry());
            donation.setDate(LocalDateTime.now());
            donation.setReference(reference);
            donation.setPaymentStatus(PaymentStatus.PENDING);

            // ✅ Get authenticated user
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                User user = (User) auth.getPrincipal();
                donation.setUser(user);
                donation.setEmail(user.getEmail());
            } else {
                log.info("User is not authenticated, using provided email");
            }

            donationRepository.save(donation);

            // For now, return a mock Paystack URL for testing
            // TODO: Implement proper Paystack integration when WebClient issues are resolved
            String mockUrl = "https://checkout.paystack.com/" + reference;
            log.info("Payment initialized successfully with reference: {} and mock URL: {}", reference, mockUrl);
            
            return mockUrl;
            
        } catch (Exception e) {
            log.error("Error initializing payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize payment: " + e.getMessage(), e);
        }
    }



    public List<DonationResponse> getDonationsByCurrentUser(String email) {
        List<Donation> donations = donationRepository.findByUserEmail(email);
        return donations.stream()
            .map(this::mapToResponse)
            .toList();
    }

    // Method to handle payment completion and update pet status
    public Donation handlePaymentSuccess(String reference) {
        Donation donation = donationRepository.findByReference(reference)
            .orElseThrow(() -> new RuntimeException("Donation not found with reference: " + reference));
        
        donation.setPaymentStatus(PaymentStatus.COMPLETED);
        
        // Automatically update pet status if donation is linked to a pet
        if (donation.getPet() != null) {
            Pet pet = donation.getPet();
            // If pet doesn't have a specific status yet, mark it as rescued through donation
            if (pet.getStatus() == PetStatus.FOR_ADOPTION || pet.getStatus() == PetStatus.IN_TREATMENT) {
                pet.setStatus(PetStatus.RESCUED_DONATION);
                petRepository.save(pet);
            }
        }
        
        return donationRepository.save(donation);
    }

    // Method to create donation linked to a specific pet
    public Donation createPetDonation(DonationRequest donationRequest, Long petId, Authentication auth) {
        String reference = UUID.randomUUID().toString();
        
        Pet pet = petRepository.findById(petId)
            .orElseThrow(() -> new RuntimeException("Pet not found with id: " + petId));

        Donation donation = new Donation();
        donation.setDonorName(donationRequest.getDonorName());
        donation.setEmail(donationRequest.getEmail());
        donation.setAmount(donationRequest.getAmount());
        donation.setGender(donationRequest.getGender());
        donation.setCountry(donationRequest.getCountry());
        donation.setDate(LocalDateTime.now());
        donation.setReference(reference);
        donation.setPaymentStatus(PaymentStatus.PENDING);
        donation.setPet(pet);

        // Get authenticated user
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            User user = (User) auth.getPrincipal();
            donation.setUser(user);
            donation.setEmail(user.getEmail());
        }

        donationRepository.save(donation);

        Map<String, Object> payload = new HashMap<>();
        payload.put("email", donation.getEmail());
        payload.put("amount", (int)(donation.getAmount() * 100));
        payload.put("reference", reference);
        payload.put("callback_url", "https://yourapp.com/payment/callback?ref=" + reference);

        return webClient.post()
                .uri(INIT_URL)
                .header("Authorization", "Bearer " + PAYSTACK_SECRET)
                .header("Content-Type", "application/json")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    String authUrl = (String) ((Map<String, Object>) response.get("data")).get("authorization_url");
                    // Save donation with payment URL
                    return donationRepository.save(donation);
                })
                .block();
    }




}