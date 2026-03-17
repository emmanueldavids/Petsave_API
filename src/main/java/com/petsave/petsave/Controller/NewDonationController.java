package com.petsave.petsave.Controller;

import com.petsave.petsave.Entity.Donation;
import com.petsave.petsave.Entity.PaymentStatus;
import com.petsave.petsave.Repository.DonationRepository;
import com.petsave.petsave.Service.EmailService;
import com.petsave.petsave.Service.NewDonationService;
import com.petsave.petsave.dto.DonationRequest;
import com.petsave.petsave.dto.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/donations")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, allowCredentials = "true")
@Slf4j
public class NewDonationController {

    private final NewDonationService newDonationService;
    private final DonationRepository donationRepository;
    private final EmailService emailService;

    @Autowired
    public NewDonationController(NewDonationService newDonationService, 
                               DonationRepository donationRepository,
                               EmailService emailService) {
        this.newDonationService = newDonationService;
        this.donationRepository = donationRepository;
        this.emailService = emailService;
    }

    @PostMapping("/initialize")
    public ResponseEntity<PaymentResponse> initializeDonation(
            @RequestBody DonationRequest donationRequest,
            Authentication authentication) {
        
        log.info("Received donation initialization request from: {}", donationRequest.getEmail());
        
        try {
            PaymentResponse response = newDonationService.initializePayment(donationRequest, authentication);
            
            if (response.isSuccess()) {
                log.info("Donation initialized successfully: {}", response.getReference());
                return ResponseEntity.ok(response);
            } else {
                log.error("Donation initialization failed: {}", response.getMessage());
                return ResponseEntity.status(500).body(response);
            }
            
        } catch (Exception e) {
            log.error("Unexpected error in donation initialization: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(PaymentResponse.error("Unexpected error: " + e.getMessage()));
        }
    }

    @PostMapping("/webhook/paystack")
    public ResponseEntity<String> handlePaystackWebhook(@RequestBody Map<String, Object> payload) {
        try {
            log.info("Received Paystack webhook: {}", payload);
            
            // Extract event and reference
            String event = (String) payload.get("event");
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            String reference = (String) data.get("reference");
            
            log.info("Processing Paystack webhook - Event: {}, Reference: {}", event, reference);
            
            if ("charge.success".equals(event)) {
                return handleSuccessfulPayment(data, reference);
            } else if ("charge.failed".equals(event)) {
                return handleFailedPayment(reference);
            } else {
                log.info("Ignoring webhook event: {}", event);
                return ResponseEntity.ok("Webhook received");
            }
            
        } catch (Exception e) {
            log.error("Error processing Paystack webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Webhook processing failed");
        }
    }

    private ResponseEntity<String> handleSuccessfulPayment(Map<String, Object> data, String reference) {
        try {
            // Find donation by reference
            Donation donation = donationRepository.findByReference(reference)
                .orElseThrow(() -> new RuntimeException("Donation not found: " + reference));
            
            // Update payment status
            donation.setPaymentStatus(PaymentStatus.COMPLETED);
            donation.setDate(LocalDateTime.now()); // Update to payment completion time
            
            // Save updated donation
            donationRepository.save(donation);
            
            // Extract payment details
            Double amount = (Double) data.get("amount");
            String customerEmail = (String) ((Map<String, Object>) data.get("customer")).get("email");
            String paidAt = data.get("paid_at") != null ? data.get("paid_at").toString() : null;
            
            // Send confirmation emails
            String paymentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            emailService.sendPaymentConfirmationEmail(
                customerEmail, 
                donation.getDonorName(), 
                amount / 100.0, // Convert from kobo to dollars
                reference
            );
            
            emailService.sendDonationReceiptEmail(
                customerEmail,
                donation.getDonorName(),
                amount / 100.0,
                reference,
                paymentDate
            );
            
            log.info("Payment processed successfully for reference: {}. Email sent to: {}", reference, customerEmail);
            return ResponseEntity.ok("Payment processed successfully");
            
        } catch (Exception e) {
            log.error("Error handling successful payment for reference {}: {}", reference, e.getMessage(), e);
            return ResponseEntity.status(500).body("Payment processing failed");
        }
    }

    private ResponseEntity<String> handleFailedPayment(String reference) {
        try {
            Donation donation = donationRepository.findByReference(reference)
                .orElseThrow(() -> new RuntimeException("Donation not found: " + reference));
            
            donation.setPaymentStatus(PaymentStatus.FAILED);
            donationRepository.save(donation);
            
            log.info("Payment failed for reference: {}", reference);
            return ResponseEntity.ok("Payment failure recorded");
            
        } catch (Exception e) {
            log.error("Error handling failed payment for reference {}: {}", reference, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed payment processing error");
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("New donation controller is working!");
    }
}
