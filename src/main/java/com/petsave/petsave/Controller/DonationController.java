package com.petsave.petsave.Controller;

import com.petsave.petsave.Service.DonationService;
import com.petsave.petsave.Service.PaymentService;
import com.petsave.petsave.dto.DonationRequest;
import com.petsave.petsave.dto.DonationResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import org.springframework.data.domain.*;


@RestController
@CrossOrigin(origins = "*") // or specify exact domains
@RequestMapping("/api/donations")
public class DonationController {

    @Autowired
    private DonationService donationService;
    @Autowired
    private PaymentService paymentService;


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


    @PostMapping
    public DonationResponse createDonation(@RequestBody DonationRequest donationRequest) {
        return donationService.createDonation(donationRequest);
    }

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

    @PostMapping("/pay")
    public ResponseEntity<Map<String, String>> pay(@RequestBody DonationRequest donationRequest) {
        String redirectUrl = paymentService.initializePayment(donationRequest);
        return ResponseEntity.ok(Map.of("redirectUrl", redirectUrl));
    }
}
    // @GetMapping("/status/{id}")
    // public ResponseEntity<Map<String, String>> getPaymentStatus(@PathVariable Long id) {
    //     String status = paymentService.getPaymentStatus(id);
    //     return ResponseEntity.ok(Map.of("status", status));
    // }
    // @GetMapping("/donor/{donorName}")
    // public Page<DonationResponse> getDonationsByDonorName(
    //     @PathVariable String donorName,
    //     @RequestParam(defaultValue = "0") int page,
    //     @RequestParam(defaultValue = "10") int size
    // ) {
    //     Sort sort = Sort.by(Sort.Order.desc("date"), Sort.Order.asc("amount"), Sort.Order.asc("donorName"));
    //     Pageable pageable = PageRequest.of(page, size, sort);
    //     return donationService.getAllDonations(donorName, pageable);
    // }
    // @GetMapping("/donor/{donorName}/count")
    // public Long getDonationCountByDonorName(@PathVariable String donorName) {
    //     return donationService.getDonationCountByDonorName(donorName);
    // }
    // @GetMapping("/donor/{donorName}/total")
    // public Double getTotalDonationsByDonorName(@PathVariable String donorName) {
    //     return donationService.getTotalDonationsByDonorName(donorName);
    // }
    // @GetMapping("/donor/{donorName}/average")
    // public Double getAverageDonationByDonorName(@PathVariable String donorName) {
    //     return donationService.getAverageDonationByDonorName(donorName);
    // }
    // @GetMapping("/donor/{donorName}/latest")
    // public DonationResponse getLatestDonationByDonorName(@PathVariable String donorName) {
    //     return donationService.getLatestDonationByDonorName(donorName);
    // }   
    // @GetMapping("/donor/{donorName}/earliest")
    // public DonationResponse getEarliestDonationByDonorName(@PathVariable String donorName) {
    //     return donationService.getEarliestDonationByDonorName(donorName);
    // }
    // @GetMapping("/donor/{donorName}/count")
    // public Long getDonationCountByDonorName(@PathVariable String donorName) {
    //     return donationService.getDonationCountByDonorName(donorName);
    // }   