package com.petsave.petsave.Controller;

import com.petsave.petsave.Service.DonationService;
import com.petsave.petsave.Service.PaymentService;
import com.petsave.petsave.Utils.JwtUtil;
import com.petsave.petsave.dto.DonationRequest;
import com.petsave.petsave.dto.DonationResponse;
import com.petsave.petsave.dto.UserDto;
import com.petsave.petsave.Entity.PaymentStatus;
import com.petsave.petsave.Entity.Donation;
import com.petsave.petsave.Repository.DonationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;




@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://127.0.0.1:3000", "http://127.0.0.1:3001"}, allowCredentials = "true")
@RequestMapping("/api/donations")
@Slf4j
public class DonationController {

    @Autowired
    private DonationService donationService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private JwtUtil jwtUtil;

    // Helper method to convert Donation to DonationResponse
    private DonationResponse convertToDonationResponse(Donation donation) {
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
        
        return response;
    }

    @GetMapping
    public Page<DonationResponse> getAllDonations(
        @RequestParam(required = false) String donorName,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Sort sort = Sort.by(Sort.Order.desc("date"), Sort.Order.asc("amount"), Sort.Order.asc("donorName"));
        Pageable pageable = PageRequest.of(page, size, sort);
        return donationService.getAllDonations(donorName, pageable);
    }


    // @PostMapping
    // public DonationResponse createDonation(@RequestBody DonationRequest donationRequest) {
    //     return donationService.createDonation(donationRequest);
    // }

    @GetMapping("/{id}")
    public DonationResponse getDonationById(@PathVariable Long id) {
        return donationService.getDonationById(id);
    }

    @PutMapping("/{id}")
    public DonationResponse updateDonation(@PathVariable Long id, @RequestBody DonationRequest donationRequest) {
        return donationService.updateDonation(id, donationRequest);
    }

    @PatchMapping("/{id}")
    public DonationResponse partialUpdateDonation(@PathVariable Long id, @RequestBody DonationRequest donationRequest) {
        return donationService.updateDonation(id, donationRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDonation(@PathVariable Long id) {
        boolean deleted = donationService.deleteDonation(id);
        if (deleted) {
            return ResponseEntity.ok().body(Map.of("message", "Donation deleted successfully"));
        } else {
            return ResponseEntity.status(404).body(Map.of("error", "Donation not found"));
        }
    }

    @GetMapping("/total")
    public Double getTotalDonations() {
        return donationService.getTotalDonations();
    }
    @GetMapping("/count")
    public Long getDonationCount() {
        return donationService.getDonationCount();
    }
    // @GetMapping("/average")
    // public Double getAverageDonation() {
    //     return donationService.getAverageDonation();
    // }

    @PostMapping
    public ResponseEntity<Map<String, String>> pay(
        @RequestBody DonationRequest donationRequest,
        Authentication auth // Injected here
    ) {
        try {
            String redirectUrl = donationService.initializePayment(donationRequest, auth);
            return ResponseEntity.ok(Map.of("redirectUrl", redirectUrl));
        } catch (Exception e) {
            log.error("Payment initialization failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(Map.of("message", "Failed to initialize payment: " + e.getMessage()));
        }
    }


    @GetMapping("/user")
    public List<DonationResponse> getUserDonations() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName(); // should be email
        return donationService.getDonationsByCurrentUser(userEmail);
    }

    // New endpoint for pet-specific donations
    @PostMapping("/pet/{petId}")
    public ResponseEntity<DonationResponse> donateToPet(
            @PathVariable Long petId,
            @RequestBody DonationRequest donationRequest) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Donation donation = donationService.createPetDonation(donationRequest, petId, auth);
            DonationResponse response = convertToDonationResponse(donation);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error donating to pet {}: {}", petId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Development-only endpoint to manually complete donations
    @PostMapping("/complete/{reference}")
    public ResponseEntity<String> completeDonation(@PathVariable String reference) {
        try {
            donationService.handlePaymentSuccess(reference);
            log.info("Manually completed donation: {}", reference);
            return ResponseEntity.ok("Donation completed successfully");
        } catch (Exception e) {
            log.error("Error completing donation {}: {}", reference, e.getMessage());
            return ResponseEntity.badRequest().body("Error completing donation: " + e.getMessage());
        }
    }

    // Test endpoint for Paystack integration
    @PostMapping("/test-paystack")
    public ResponseEntity<Map<String, Object>> testPaystack() {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("email", "test@example.com");
            payload.put("amount", 2500);
            payload.put("reference", "test_ref_" + System.currentTimeMillis());
            payload.put("callback_url", "http://localhost:3000/callback");

            log.info("Testing Paystack with payload: {}", payload);

            WebClient webClient = WebClient.create();
            Map<String, Object> response = webClient.post()
                    .uri("https://api.paystack.co/transaction/initialize")
                    .header("Authorization", "Bearer sk_test_531fdff89f75fd24201eeeee9cb265fed66981a7")
                    .header("Content-Type", "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            log.info("Paystack test response: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Paystack test failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(Map.of("error", e.getMessage()));
        }
    }

    // Webhook endpoint for payment success
    @PostMapping("/webhook/success")
    public ResponseEntity<Map<String, String>> handlePaymentSuccess(@RequestBody Map<String, String> payload) {
        String reference = payload.get("reference");
        donationService.handlePaymentSuccess(reference);
        return ResponseEntity.ok(Map.of("message", "Payment processed successfully"));
    }
   
}