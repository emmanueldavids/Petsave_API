package com.petsave.petsave.Controller;

import com.petsave.petsave.Service.DonationService;
import com.petsave.petsave.Service.NewDonationService;
import com.petsave.petsave.dto.DonationRequest;
import com.petsave.petsave.dto.DonationResponse;
import com.petsave.petsave.dto.PaymentResponse;
import com.petsave.petsave.Entity.Donation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/donations")
@Slf4j
public class DonationController {

    @Autowired
    private DonationService donationService;

    @Autowired
    private NewDonationService newDonationService;

    private DonationResponse toResponse(Donation donation) {
        DonationResponse r = new DonationResponse();
        r.setId(donation.getId());
        r.setDonorName(donation.getDonorName());
        r.setEmail(donation.getEmail());
        r.setAmount(donation.getAmount());
        r.setDate(donation.getDate());
        r.setGender(donation.getGender());
        r.setCountry(donation.getCountry());
        r.setPaymentStatus(donation.getPaymentStatus());
        r.setReference(donation.getReference());
        return r;
    }

    @GetMapping
    public Page<DonationResponse> getAllDonations(
            @RequestParam(required = false) String donorName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("date"), Sort.Order.asc("donorName")));
        return donationService.getAllDonations(donorName, pageable);
    }

    @GetMapping("/{id}")
    public DonationResponse getDonationById(@PathVariable Long id) {
        return donationService.getDonationById(id);
    }

    @PutMapping("/{id}")
    public DonationResponse updateDonation(@PathVariable Long id, @RequestBody DonationRequest request) {
        return donationService.updateDonation(id, request);
    }

    @PatchMapping("/{id}")
    public DonationResponse partialUpdateDonation(@PathVariable Long id, @RequestBody DonationRequest request) {
        return donationService.updateDonation(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDonation(@PathVariable Long id) {
        boolean deleted = donationService.deleteDonation(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Donation deleted successfully"));
        }
        return ResponseEntity.status(404).body(Map.of("error", "Donation not found"));
    }

    @GetMapping("/total")
    public Double getTotalDonations() {
        return donationService.getTotalDonations();
    }

    @GetMapping("/count")
    public Long getDonationCount() {
        return donationService.getDonationCount();
    }

    @GetMapping("/user")
    public List<DonationResponse> getUserDonations() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return donationService.getDonationsByCurrentUser(auth.getName());
    }

    @PostMapping("/pet/{petId}")
    public ResponseEntity<DonationResponse> donateToPet(
            @PathVariable Long petId,
            @RequestBody DonationRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Donation donation = donationService.createPetDonation(request, petId, auth);
        return ResponseEntity.ok(toResponse(donation));
    }

    @PostMapping("/initialize")
    public ResponseEntity<PaymentResponse> initializeDonation(
            @RequestBody DonationRequest request,
            Authentication authentication) {
        log.info("Donation initialization request from: {}", request.getEmail());
        PaymentResponse response = newDonationService.initializePayment(request, authentication);
        if (response.isSuccess()) {
            log.info("Donation initialized, reference: {}", response.getReference());
            return ResponseEntity.ok(response);
        }
        log.error("Donation initialization failed: {}", response.getMessage());
        return ResponseEntity.status(500).body(response);
    }
}
