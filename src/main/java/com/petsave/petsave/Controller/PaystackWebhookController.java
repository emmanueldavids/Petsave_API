package com.petsave.petsave.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petsave.petsave.Config.NotificationWebSocketHandler;
import com.petsave.petsave.Entity.PaymentStatus;
import com.petsave.petsave.Entity.PetSitting;
import com.petsave.petsave.Entity.PetSittingPaymentStatus;
import com.petsave.petsave.Entity.PetSittingStatus;
import com.petsave.petsave.Repository.DonationRepository;
import com.petsave.petsave.Repository.PetSittingRepository;
import com.petsave.petsave.Service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/paystack")
@Slf4j
public class PaystackWebhookController {

    @Value("${paystack.secret.key}")
    private String paystackSecret;

    private final DonationRepository donationRepository;
    private final PetSittingRepository petSittingRepository;
    private final EmailService emailService;
    private final NotificationWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PaystackWebhookController(DonationRepository donationRepository,
                                      PetSittingRepository petSittingRepository,
                                      EmailService emailService,
                                      NotificationWebSocketHandler webSocketHandler) {
        this.donationRepository = donationRepository;
        this.petSittingRepository = petSittingRepository;
        this.emailService = emailService;
        this.webSocketHandler = webSocketHandler;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String rawBody,
            @RequestHeader("x-paystack-signature") String signature) {
        try {
            String computed = computeHmacSHA512(rawBody, paystackSecret);
            if (!computed.equals(signature)) {
                log.warn("Invalid Paystack webhook signature");
                return ResponseEntity.status(403).body("Invalid signature");
            }

            Map<String, Object> payload = objectMapper.readValue(rawBody, Map.class);
            String event = (String) payload.get("event");
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            String reference = (String) data.get("reference");

            log.info("Paystack webhook received — event: {}, reference: {}", event, reference);

            if ("charge.success".equals(event)) {
                handleSuccess(data, reference);
            } else if ("charge.failed".equals(event)) {
                handleFailure(reference);
            }

            return ResponseEntity.ok("Webhook handled");

        } catch (Exception e) {
            log.error("Webhook processing error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Webhook processing error");
        }
    }

    private void handleSuccess(Map<String, Object> data, String reference) {
        boolean matchedDonation = donationRepository.findByReference(reference).map(donation -> {
            donation.setPaymentStatus(PaymentStatus.COMPLETED);
            donation.setDate(LocalDateTime.now());
            donationRepository.save(donation);

            // Extract customer details from Paystack payload
            Map<String, Object> customer = (Map<String, Object>) data.get("customer");
            String customerEmail = customer != null ? (String) customer.get("email") : donation.getEmail();
            Number rawAmount = (Number) data.get("amount");
            double amount = rawAmount != null ? rawAmount.doubleValue() / 100.0 : donation.getAmount();
            String paymentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Send confirmation emails after payment is actually completed
            try {
                emailService.sendPaymentConfirmationEmail(customerEmail, donation.getDonorName(), amount, reference);
                emailService.sendDonationReceiptEmail(customerEmail, donation.getDonorName(), amount, reference, paymentDate);
                emailService.sendAdminDonationNotification(donation.getDonorName(), customerEmail, amount, reference);
                log.info("Payment confirmation emails sent for reference: {}", reference);
            } catch (Exception e) {
                log.error("Failed to send donation emails for reference {}: {}", reference, e.getMessage());
            }
            return true;
        }).orElse(false);

        if (!matchedDonation) {
            handlePetSittingSuccess(reference);
        }
    }

    private void handlePetSittingSuccess(String reference) {
        petSittingRepository.findByPaymentReference(reference).ifPresent(booking -> {
            booking.setPaymentStatus(PetSittingPaymentStatus.PAID);
            booking.setStatus(PetSittingStatus.CONFIRMED);
            petSittingRepository.save(booking);
            log.info("Pet sitting booking {} confirmed after payment, reference: {}", booking.getId(), reference);

            notify(booking, "BOOKING_CONFIRMED", "Payment received — booking is confirmed.");
        });
    }

    private void handlePetSittingFailure(String reference) {
        petSittingRepository.findByPaymentReference(reference).ifPresent(booking -> {
            booking.setPaymentStatus(PetSittingPaymentStatus.FAILED);
            booking.setStatus(PetSittingStatus.CANCELLED);
            petSittingRepository.save(booking);
            log.info("Pet sitting booking {} cancelled after failed payment, reference: {}", booking.getId(), reference);

            notify(booking, "BOOKING_PAYMENT_FAILED", "Payment failed — booking was cancelled.");
        });
    }

    private void notify(PetSitting booking, String type, String message) {
        try {
            Map<String, Object> payload = Map.of(
                    "type", type,
                    "message", message,
                    "bookingId", booking.getId(),
                    "timestamp", System.currentTimeMillis()
            );
            webSocketHandler.sendToUser(booking.getOwner().getEmail(), payload);
            webSocketHandler.sendToUser(booking.getSitter().getEmail(), payload);
        } catch (Exception e) {
            log.warn("Failed to send booking payment notification: {}", e.getMessage());
        }
    }

    private void handleFailure(String reference) {
        boolean matchedDonation = donationRepository.findByReference(reference).map(donation -> {
            donation.setPaymentStatus(PaymentStatus.FAILED);
            donationRepository.save(donation);
            log.info("Payment marked failed for reference: {}", reference);
            return true;
        }).orElse(false);

        if (!matchedDonation) {
            handlePetSittingFailure(reference);
        }
    }

    private String computeHmacSHA512(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
