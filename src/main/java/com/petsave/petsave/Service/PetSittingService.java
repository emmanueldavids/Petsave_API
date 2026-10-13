package com.petsave.petsave.Service;

import com.petsave.petsave.Config.NotificationWebSocketHandler;
import com.petsave.petsave.Entity.*;
import com.petsave.petsave.Repository.*;
import com.petsave.petsave.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PetSittingService {

    private static final String PAYSTACK_INIT_URL = "https://api.paystack.co/transaction/initialize";
    private static final String PAYSTACK_REFUND_URL = "https://api.paystack.co/refund";
    private static final String PAYSTACK_VERIFY_URL = "https://api.paystack.co/transaction/verify/";

    // 4xx responses are deterministic (bad request/validation) — retrying wastes time and
    // never succeeds. Only retry on network-level failures or genuine 5xx server errors.
    private static final Retry PAYSTACK_RETRY = Retry.backoff(3, Duration.ofMillis(300))
            .filter(ex -> !(ex instanceof WebClientResponseException wcre) || wcre.getStatusCode().is5xxServerError());

    private String describeError(Exception e) {
        if (e instanceof WebClientResponseException wcre) {
            return wcre.getStatusCode() + " " + wcre.getResponseBodyAsString();
        }
        return e.getMessage();
    }

    @Value("${paystack.secret.key}")
    private String paystackSecretKey;

    @Value("${app.frontend.url:https://petsave-frontend.vercel.app}")
    private String frontendUrl;

    @Value("${petsitting.platform-fee-percentage:15}")
    private double platformFeePercentage;

    private final PetSittingRepository petSittingRepository;
    private final PetSitterRepository petSitterRepository;
    private final OwnedPetRepository ownedPetRepository;
    private final SitterWalletRepository sitterWalletRepository;
    private final PetSittingReviewRepository petSittingReviewRepository;
    private final UserRepository userRepository;
    private final PaystackTransferService paystackTransferService;
    private final NotificationWebSocketHandler webSocketHandler;
    private final NotificationService notificationService;
    private final WebClient.Builder webClientBuilder;

    public PetSittingResponse bookSitting(BookSittingRequest request) {
        User owner = getCurrentUser();

        PetSitter sitterProfile = petSitterRepository.findById(request.getSitterId())
                .orElseThrow(() -> new RuntimeException("Sitter not found with id: " + request.getSitterId()));
        if (sitterProfile.getStatus() != PetSitterStatus.ACTIVE) {
            throw new RuntimeException("This sitter is not currently accepting bookings");
        }
        if (sitterProfile.getUser().getId().equals(owner.getId())) {
            throw new RuntimeException("You cannot book yourself as a sitter");
        }

        OwnedPet pet = ownedPetRepository.findById(request.getPetId())
                .orElseThrow(() -> new RuntimeException("Pet not found with id: " + request.getPetId()));
        if (!pet.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You can only book sitting for your own pet");
        }

        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new RuntimeException("endDate must be after startDate");
        }
        if (request.getStartDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("startDate cannot be in the past");
        }

        int maxPets = sitterProfile.getMaxPetsAtOnce() != null ? sitterProfile.getMaxPetsAtOnce() : 1;
        long overlapping = petSittingRepository.countOverlappingBookings(sitterProfile.getUser().getId(),
                List.of(PetSittingStatus.ACCEPTED, PetSittingStatus.CONFIRMED, PetSittingStatus.ONGOING),
                request.getStartDate(), request.getEndDate());
        if (overlapping >= maxPets) {
            throw new RuntimeException("This sitter is already fully booked for the requested dates");
        }

        long petOverlapping = petSittingRepository.countOverlappingBookingsForPet(pet.getId(),
                List.of(PetSittingStatus.ACCEPTED, PetSittingStatus.CONFIRMED, PetSittingStatus.ONGOING),
                request.getStartDate(), request.getEndDate());
        if (petOverlapping > 0) {
            throw new RuntimeException("This pet already has a confirmed sitting booked for overlapping dates");
        }

        long days = ChronoUnit.DAYS.between(request.getStartDate().toLocalDate(), request.getEndDate().toLocalDate());
        if (days < 1) {
            days = 1;
        }
        BigDecimal totalAmount = sitterProfile.getRatePerDay().multiply(BigDecimal.valueOf(days));

        PetSitting booking = new PetSitting();
        booking.setOwner(owner);
        booking.setSitter(sitterProfile.getUser());
        booking.setPet(pet);
        booking.setStartDate(request.getStartDate());
        booking.setEndDate(request.getEndDate());
        booking.setTotalAmount(totalAmount);
        booking.setStatus(PetSittingStatus.REQUESTED);
        booking.setPaymentStatus(PetSittingPaymentStatus.PENDING);
        booking.setOwnerNotes(request.getOwnerNotes());

        PetSitting saved = petSittingRepository.save(booking);
        notifyParticipant(booking.getSitter(), "BOOKING_REQUESTED", "You have a new pet sitting request.", booking);
        return mapToResponse(saved);
    }

    public PetSittingResponse acceptBooking(Long id) {
        User currentUser = getCurrentUser();
        PetSitting booking = petSittingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        if (!booking.getSitter().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the sitter can accept this booking");
        }
        if (booking.getStatus() != PetSittingStatus.REQUESTED) {
            throw new RuntimeException("This booking is not awaiting acceptance");
        }

        booking.setStatus(PetSittingStatus.ACCEPTED);
        PetSitting saved = petSittingRepository.save(booking);
        notifyParticipant(booking.getOwner(), "BOOKING_ACCEPTED", "Your sitter accepted the booking request. Complete payment to confirm.", booking);
        return mapToResponse(saved);
    }

    public PetSittingResponse rejectBooking(Long id) {
        User currentUser = getCurrentUser();
        PetSitting booking = petSittingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        if (!booking.getSitter().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the sitter can reject this booking");
        }
        if (booking.getStatus() != PetSittingStatus.REQUESTED) {
            throw new RuntimeException("This booking is not awaiting acceptance");
        }

        booking.setStatus(PetSittingStatus.REJECTED);
        PetSitting saved = petSittingRepository.save(booking);
        notifyParticipant(booking.getOwner(), "BOOKING_REJECTED", "Your sitter declined the booking request.", booking);
        return mapToResponse(saved);
    }

    public PetSittingResponse payForBooking(Long id, String callbackUrl) {
        User currentUser = getCurrentUser();
        PetSitting booking = petSittingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        if (!booking.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the owner can pay for this booking");
        }
        if (booking.getStatus() != PetSittingStatus.ACCEPTED) {
            throw new RuntimeException("This booking must be accepted by the sitter before payment");
        }

        String reference = "PETSIT_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        booking.setPaymentReference(reference);
        PetSitting saved = petSittingRepository.save(booking);

        String authorizationUrl = initializePaystackPayment(currentUser, booking.getTotalAmount(), reference, callbackUrl);

        PetSittingResponse response = mapToResponse(saved);
        response.setPaymentAuthorizationUrl(authorizationUrl);
        return response;
    }

    /**
     * Manually reconciles a booking's payment status against Paystack directly,
     * instead of waiting for the webhook. Mainly useful in local dev, where Paystack's
     * servers can't reach localhost to deliver the webhook at all — but also a
     * reasonable production safety net, since webhook delivery isn't 100% guaranteed.
     * Safe to call repeatedly: no-ops if the booking's already past CONFIRMED/CANCELLED.
     */
    public PetSittingResponse verifyPayment(Long id) {
        User currentUser = getCurrentUser();
        PetSitting booking = petSittingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        boolean isParticipant = booking.getOwner().getId().equals(currentUser.getId())
                || booking.getSitter().getId().equals(currentUser.getId());
        if (!isParticipant && !isAdmin(currentUser)) {
            throw new RuntimeException("You are not a participant in this booking");
        }
        if (booking.getPaymentReference() == null) {
            throw new RuntimeException("This booking has no payment to verify yet");
        }

        String paystackStatus = fetchPaystackTransactionStatus(booking.getPaymentReference());
        if ("success".equalsIgnoreCase(paystackStatus)) {
            applyPaymentSuccess(booking);
        } else if ("failed".equalsIgnoreCase(paystackStatus) || "reversed".equalsIgnoreCase(paystackStatus)) {
            applyPaymentFailure(booking);
        }
        // Anything else (e.g. "abandoned" — Paystack's status for "checkout page visited, not yet
        // completed" — "pending", "processing", or an API hiccup) — leave the booking exactly as it
        // is. "abandoned" specifically is NOT treated as a hard failure: it can appear almost
        // immediately after /pay if the owner hasn't finished checkout yet, and auto-cancelling here
        // would be premature — they might still come back and complete payment on the same reference.

        PetSitting refreshed = petSittingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        return mapToResponse(refreshed);
    }

    /**
     * Shared by both the Paystack webhook and the manual verify-payment endpoint,
     * so the two code paths can never drift apart.
     */
    public void applyPaymentSuccessByReference(String reference) {
        petSittingRepository.findByPaymentReference(reference).ifPresent(this::applyPaymentSuccess);
    }

    public void applyPaymentFailureByReference(String reference) {
        petSittingRepository.findByPaymentReference(reference).ifPresent(this::applyPaymentFailure);
    }

    private void applyPaymentSuccess(PetSitting booking) {
        if (booking.getStatus() == PetSittingStatus.CONFIRMED || booking.getPaymentStatus() == PetSittingPaymentStatus.PAID) {
            return;
        }
        booking.setPaymentStatus(PetSittingPaymentStatus.PAID);
        booking.setStatus(PetSittingStatus.CONFIRMED);
        petSittingRepository.save(booking);
        log.info("Pet sitting booking {} confirmed after payment, reference: {}", booking.getId(), booking.getPaymentReference());
        notifyParticipant(booking.getOwner(), "BOOKING_CONFIRMED", "Payment received — booking is confirmed.", booking);
        notifyParticipant(booking.getSitter(), "BOOKING_CONFIRMED", "Payment received — booking is confirmed.", booking);
    }

    private void applyPaymentFailure(PetSitting booking) {
        if (booking.getStatus() == PetSittingStatus.CANCELLED) {
            return;
        }
        booking.setPaymentStatus(PetSittingPaymentStatus.FAILED);
        booking.setStatus(PetSittingStatus.CANCELLED);
        petSittingRepository.save(booking);
        log.info("Pet sitting booking {} cancelled after failed payment, reference: {}", booking.getId(), booking.getPaymentReference());
        notifyParticipant(booking.getOwner(), "BOOKING_PAYMENT_FAILED", "Payment failed — booking was cancelled.", booking);
        notifyParticipant(booking.getSitter(), "BOOKING_PAYMENT_FAILED", "Payment failed — booking was cancelled.", booking);
    }

    private String fetchPaystackTransactionStatus(String reference) {
        try {
            Map<String, Object> response = webClientBuilder.build()
                    .get()
                    .uri(PAYSTACK_VERIFY_URL + reference)
                    .header("Authorization", "Bearer " + paystackSecretKey)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .retryWhen(PAYSTACK_RETRY)
                    .block();

            if (response != null && Boolean.TRUE.equals(response.get("status"))) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                return (String) data.get("status");
            }
            log.error("Paystack verify call failed for reference {}: {}", reference, response);
            return "unknown";
        } catch (Exception e) {
            log.error("Paystack verify error for reference {}: {}", reference, describeError(e), e);
            return "unknown";
        }
    }

    public List<PetSittingResponse> listMyBookings() {
        User currentUser = getCurrentUser();
        return petSittingRepository.findByParticipant(currentUser.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PetSittingResponse getBooking(Long id) {
        User currentUser = getCurrentUser();
        PetSitting booking = getBookingForParticipant(id, currentUser);
        return mapToResponse(booking);
    }

    public PetSittingResponse confirmBooking(Long id) {
        User currentUser = getCurrentUser();
        PetSitting booking = petSittingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        if (!booking.getSitter().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the sitter can confirm this booking");
        }
        if (booking.getStatus() != PetSittingStatus.CONFIRMED) {
            throw new RuntimeException("Only a paid, confirmed booking can be started — current status: " + booking.getStatus());
        }
        booking.setStatus(PetSittingStatus.ONGOING);
        PetSitting saved = petSittingRepository.save(booking);
        notifyParticipant(booking.getOwner(), "BOOKING_ONGOING", "Your sitter has confirmed and started the booking.", booking);
        return mapToResponse(saved);
    }

    public PetSittingResponse cancelBooking(Long id) {
        User currentUser = getCurrentUser();
        PetSitting booking = petSittingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        boolean isParticipant = booking.getOwner().getId().equals(currentUser.getId())
                || booking.getSitter().getId().equals(currentUser.getId());
        if (!isParticipant && !isAdmin(currentUser)) {
            throw new RuntimeException("Only the owner or sitter can cancel this booking");
        }
        if (booking.getStatus() != PetSittingStatus.REQUESTED && booking.getStatus() != PetSittingStatus.ACCEPTED
                && booking.getStatus() != PetSittingStatus.CONFIRMED) {
            throw new RuntimeException("A booking that is " + booking.getStatus() + " can no longer be cancelled");
        }

        booking.setStatus(PetSittingStatus.CANCELLED);

        if (booking.getPaymentStatus() == PetSittingPaymentStatus.PAID) {
            boolean refunded = refundPayment(booking.getPaymentReference());
            if (refunded) {
                booking.setPaymentStatus(PetSittingPaymentStatus.REFUNDED);
            } else {
                log.warn("Booking {} cancelled but the Paystack refund could not be processed — " +
                        "paymentStatus stays PAID pending manual refund.", booking.getId());
            }
        }
        // If paymentStatus was already RELEASED (payout already sent to the sitter's bank),
        // there's nothing to auto-refund — that requires manual recovery outside this system.

        PetSitting saved = petSittingRepository.save(booking);

        User other = booking.getOwner().getId().equals(currentUser.getId()) ? booking.getSitter() : booking.getOwner();
        notifyParticipant(other, "BOOKING_CANCELLED", "A pet sitting booking was cancelled.", booking);
        return mapToResponse(saved);
    }

    /**
     * Admins can force-complete a booking at any point (except one already finished).
     * The owner can also complete it themselves, but only once it's ONGOING — they're
     * the one who actually knows when they've picked their pet back up. The sitter
     * cannot self-complete: that would let them trigger their own payout unilaterally.
     */
    public PetSittingResponse completeBooking(Long id) {
        User currentUser = getCurrentUser();
        PetSitting booking = petSittingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));

        boolean isAdmin = isAdmin(currentUser);
        boolean isOwner = booking.getOwner().getId().equals(currentUser.getId());

        if (isAdmin) {
            if (booking.getStatus() == PetSittingStatus.COMPLETED || booking.getStatus() == PetSittingStatus.CANCELLED
                    || booking.getStatus() == PetSittingStatus.REJECTED) {
                throw new RuntimeException("A booking that is " + booking.getStatus() + " cannot be completed again");
            }
        } else if (isOwner) {
            if (booking.getStatus() != PetSittingStatus.ONGOING) {
                throw new RuntimeException("You can only mark a booking complete once the sitter has confirmed and started it");
            }
        } else {
            throw new RuntimeException("Only the owner can mark this booking complete");
        }

        completeBookingInternal(booking);
        return mapToResponse(booking);
    }

    public PetSittingReviewResponse leaveReview(Long bookingId, PetSittingReviewRequest request) {
        User currentUser = getCurrentUser();
        PetSitting booking = petSittingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        boolean isOwner = booking.getOwner().getId().equals(currentUser.getId());
        boolean isSitter = booking.getSitter().getId().equals(currentUser.getId());
        if (!isOwner && !isSitter) {
            throw new RuntimeException("Only the owner or sitter of this booking can leave a review");
        }
        if (booking.getStatus() != PetSittingStatus.COMPLETED) {
            throw new RuntimeException("You can only review a completed booking");
        }
        if (petSittingReviewRepository.existsByBookingAndReviewer(booking, currentUser)) {
            throw new RuntimeException("You have already reviewed this booking");
        }

        User reviewee = isOwner ? booking.getSitter() : booking.getOwner();

        PetSittingReview review = new PetSittingReview();
        review.setBooking(booking);
        review.setReviewer(currentUser);
        review.setReviewee(reviewee);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        PetSittingReview saved = petSittingReviewRepository.save(review);

        // Only sitters have a public rating aggregate to update — reviewing an owner is
        // stored for the record but there's no "owner profile" entity to roll it up into.
        if (isOwner) {
            petSitterRepository.findByUserId(reviewee.getId()).ifPresent(sitterProfile -> {
                int oldCount = sitterProfile.getTotalReviews();
                float oldAvg = sitterProfile.getAverageRating();
                float newAvg = ((oldAvg * oldCount) + request.getRating()) / (oldCount + 1);
                sitterProfile.setAverageRating(newAvg);
                sitterProfile.setTotalReviews(oldCount + 1);
                petSitterRepository.save(sitterProfile);
            });
        }

        return mapToReviewResponse(saved);
    }

    public PetSittingResponse updateNotes(Long id, String notes) {
        User currentUser = getCurrentUser();
        PetSitting booking = petSittingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));

        boolean isOwner = booking.getOwner().getId().equals(currentUser.getId());
        boolean isSitter = booking.getSitter().getId().equals(currentUser.getId());
        if (isOwner) {
            booking.setOwnerNotes(notes);
        } else if (isSitter) {
            booking.setSitterNotes(notes);
        } else {
            throw new RuntimeException("Only the owner or sitter can update notes on this booking");
        }

        PetSitting saved = petSittingRepository.save(booking);
        return mapToResponse(saved);
    }

    public PetSittingResponse raiseDispute(Long id, String reason) {
        User currentUser = getCurrentUser();
        PetSitting booking = petSittingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));

        boolean isOwner = booking.getOwner().getId().equals(currentUser.getId());
        boolean isSitter = booking.getSitter().getId().equals(currentUser.getId());
        if (!isOwner && !isSitter) {
            throw new RuntimeException("Only the owner or sitter of this booking can raise a dispute");
        }
        if (booking.getStatus() != PetSittingStatus.ONGOING && booking.getStatus() != PetSittingStatus.COMPLETED) {
            throw new RuntimeException("A dispute can only be raised once the sitting has started or completed — current status: " + booking.getStatus());
        }

        String raisedBy = isOwner ? "owner" : "sitter";
        booking.setDisputeReason("Raised by " + raisedBy + ": " + (reason != null && !reason.isBlank() ? reason : "(no reason given)"));
        booking.setStatus(PetSittingStatus.DISPUTED);
        PetSitting saved = petSittingRepository.save(booking);

        User other = isOwner ? booking.getSitter() : booking.getOwner();
        notifyParticipant(other, "BOOKING_DISPUTED", "A dispute was raised on a pet sitting booking.", booking);
        return mapToResponse(saved);
    }

    /**
     * Admin-only. RELEASE_TO_SITTER completes the booking normally (attempts the real payout).
     * REFUND_OWNER cancels it and attempts a real Paystack refund — but only if the payment
     * hasn't already been released to the sitter's bank; that can't be auto-reversed.
     */
    public PetSittingResponse resolveDispute(Long id, String resolution) {
        User currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) {
            throw new RuntimeException("Only admins can resolve a dispute");
        }
        PetSitting booking = petSittingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        if (booking.getStatus() != PetSittingStatus.DISPUTED) {
            throw new RuntimeException("This booking is not currently disputed");
        }

        if ("RELEASE_TO_SITTER".equalsIgnoreCase(resolution)) {
            completeBookingInternal(booking);
        } else if ("REFUND_OWNER".equalsIgnoreCase(resolution)) {
            if (booking.getPaymentStatus() == PetSittingPaymentStatus.RELEASED) {
                throw new RuntimeException("Cannot auto-refund — this booking's payment was already released to the sitter; resolve manually outside the system");
            }
            booking.setStatus(PetSittingStatus.CANCELLED);
            if (booking.getPaymentStatus() == PetSittingPaymentStatus.PAID) {
                boolean refunded = refundPayment(booking.getPaymentReference());
                if (refunded) {
                    booking.setPaymentStatus(PetSittingPaymentStatus.REFUNDED);
                } else {
                    log.warn("Dispute resolution refund failed for booking {} — paymentStatus stays PAID pending manual refund.", booking.getId());
                }
            }
            petSittingRepository.save(booking);
            notifyParticipant(booking.getOwner(), "DISPUTE_RESOLVED", "Your dispute was resolved: refunded.", booking);
            notifyParticipant(booking.getSitter(), "DISPUTE_RESOLVED", "A dispute was resolved: owner refunded.", booking);
        } else {
            throw new RuntimeException("Invalid resolution: " + resolution + " (expected RELEASE_TO_SITTER or REFUND_OWNER)");
        }

        return mapToResponse(booking);
    }

    /**
     * Hourly sweep: bookings whose sitting period ended 24+ hours ago and are still
     * CONFIRMED (sitter never explicitly started it) or ONGOING get auto-completed.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void autoCompleteBookings() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<PetSitting> due = petSittingRepository.findByStatusInAndEndDateBefore(
                List.of(PetSittingStatus.CONFIRMED, PetSittingStatus.ONGOING), cutoff);
        for (PetSitting booking : due) {
            try {
                completeBookingInternal(booking);
            } catch (Exception e) {
                log.error("Auto-complete failed for booking {}: {}", booking.getId(), e.getMessage(), e);
            }
        }
    }

    private void completeBookingInternal(PetSitting booking) {
        booking.setStatus(PetSittingStatus.COMPLETED);
        attemptPayoutRelease(booking);
        petSittingRepository.save(booking);
        notifyParticipant(booking.getSitter(), "BOOKING_COMPLETED", "A booking was marked completed.", booking);
        notifyParticipant(booking.getOwner(), "BOOKING_COMPLETED", "Your pet sitting booking was marked completed.", booking);
    }

    /**
     * Attempts to release a completed booking's held payment to the sitter's wallet/bank.
     * Returns true only if Paystack confirmed the transfer — leaves paymentStatus as PAID
     * (not RELEASED) on failure so it can be retried later via {@link #retryPayout(Long)}.
     */
    private boolean attemptPayoutRelease(PetSitting booking) {
        BigDecimal netAmount = booking.getTotalAmount()
                .multiply(BigDecimal.valueOf(100 - platformFeePercentage))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        PetSitter sitterProfile = petSitterRepository.findByUserId(booking.getSitter().getId()).orElse(null);
        boolean released = false;
        if (sitterProfile != null) {
            String recipientCode = paystackTransferService.ensureRecipient(sitterProfile);
            if (recipientCode != null) {
                if (!recipientCode.equals(sitterProfile.getPaystackRecipientCode())) {
                    sitterProfile.setPaystackRecipientCode(recipientCode);
                    petSitterRepository.save(sitterProfile);
                }
                released = paystackTransferService.transfer(recipientCode, netAmount,
                        "Pet sitting payout - booking #" + booking.getId());
            }
        }

        if (released) {
            booking.setPaymentStatus(PetSittingPaymentStatus.RELEASED);
            SitterWallet wallet = sitterWalletRepository.findByUserId(booking.getSitter().getId())
                    .orElseGet(() -> {
                        SitterWallet w = new SitterWallet();
                        w.setUser(booking.getSitter());
                        return w;
                    });
            wallet.setBalance(wallet.getBalance().add(netAmount));
            sitterWalletRepository.save(wallet);
        } else {
            log.warn("Booking {} completed but payout could not be released automatically — " +
                    "sitter has no valid payout details on file. paymentStatus stays PAID pending manual payout.", booking.getId());
        }
        return released;
    }

    /**
     * Admin-only: retries releasing a completed booking's payout after it initially failed
     * (e.g. sitter's payout details were missing/invalid at completion time and have since
     * been fixed). No-op restrictions: booking must be COMPLETED and not already RELEASED.
     */
    public PetSittingResponse retryPayout(Long id) {
        User currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) {
            throw new RuntimeException("Only admins can retry a payout");
        }
        PetSitting booking = petSittingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        if (booking.getStatus() != PetSittingStatus.COMPLETED) {
            throw new RuntimeException("Only a completed booking's payout can be retried — current status: " + booking.getStatus());
        }
        if (booking.getPaymentStatus() == PetSittingPaymentStatus.RELEASED) {
            throw new RuntimeException("This booking's payout has already been released");
        }
        boolean released = attemptPayoutRelease(booking);
        PetSitting saved = petSittingRepository.save(booking);
        if (released) {
            notifyParticipant(booking.getSitter(), "PAYOUT_RELEASED", "Your payout for a completed booking has been released.", booking);
        } else {
            throw new RuntimeException("Payout retry failed — sitter still has no valid payout details on file");
        }
        return mapToResponse(saved);
    }

    private PetSitting getBookingForParticipant(Long id, User user) {
        PetSitting booking = petSittingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        boolean isParticipant = booking.getOwner().getId().equals(user.getId())
                || booking.getSitter().getId().equals(user.getId());
        if (!isParticipant && !isAdmin(user)) {
            throw new RuntimeException("You are not a participant in this booking");
        }
        return booking;
    }

    private String initializePaystackPayment(User owner, BigDecimal amount, String reference, String callbackUrl) {
        try {
            String resolvedCallback = (callbackUrl != null && !callbackUrl.isBlank())
                    ? callbackUrl
                    : frontendUrl + "/pet-sittings/callback?ref=" + reference;

            Map<String, Object> payload = new HashMap<>();
            payload.put("email", owner.getEmail());
            payload.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue());
            payload.put("reference", reference);
            payload.put("callback_url", resolvedCallback);

            Map<String, Object> response = webClientBuilder.build()
                    .post()
                    .uri(PAYSTACK_INIT_URL)
                    .header("Authorization", "Bearer " + paystackSecretKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .retryWhen(PAYSTACK_RETRY)
                    .block();

            if (response != null && Boolean.TRUE.equals(response.get("status"))) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                return (String) data.get("authorization_url");
            }
            log.error("Paystack initialize failed for reference {}: {}", reference, response);
            throw new RuntimeException("Failed to initialize payment with Paystack");
        } catch (Exception e) {
            log.error("Paystack initialize error for reference {}: {}", reference, describeError(e), e);
            throw new RuntimeException("Failed to initialize payment: " + e.getMessage());
        }
    }

    /**
     * Refunds the full amount of a Paystack charge. Returns true only if Paystack
     * confirmed the refund — never claim a refund happened unless this returns true.
     */
    private boolean refundPayment(String reference) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("transaction", reference);

            Map<String, Object> response = webClientBuilder.build()
                    .post()
                    .uri(PAYSTACK_REFUND_URL)
                    .header("Authorization", "Bearer " + paystackSecretKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .retryWhen(PAYSTACK_RETRY)
                    .block();

            if (response != null && Boolean.TRUE.equals(response.get("status"))) {
                log.info("Paystack refund succeeded for reference {}: {}", reference, response.get("data"));
                return true;
            }
            log.error("Paystack refund failed for reference {}: {}", reference, response);
            return false;
        } catch (Exception e) {
            log.error("Paystack refund error for reference {}: {}", reference, describeError(e), e);
            return false;
        }
    }

    private void notifyParticipant(User user, String type, String message, PetSitting booking) {
        try {
            webSocketHandler.sendToUser(user.getEmail(), Map.of(
                    "type", type,
                    "message", message,
                    "bookingId", booking.getId(),
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.warn("Failed to send booking notification to {}: {}", user.getEmail(), e.getMessage());
        }

        try {
            notificationService.notify(user.getEmail(), Notification.NotificationType.valueOf(type), message);
        } catch (IllegalArgumentException e) {
            log.warn("No matching NotificationType for pet-sitting event '{}' — skipping persistence", type);
        }
    }

    private boolean isAdmin(User user) {
        return "ADMIN".equalsIgnoreCase(user.getRole());
    }

    private PetSittingResponse mapToResponse(PetSitting booking) {
        return PetSittingResponse.builder()
                .id(booking.getId())
                .owner(mapToUserResponse(booking.getOwner()))
                .sitter(mapToUserResponse(booking.getSitter()))
                .pet(mapToPetResponse(booking.getPet()))
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus().name())
                .paymentReference(booking.getPaymentReference())
                .paymentStatus(booking.getPaymentStatus().name())
                .ownerNotes(booking.getOwnerNotes())
                .sitterNotes(booking.getSitterNotes())
                .disputeReason(booking.getDisputeReason())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    private OwnedPetResponse mapToPetResponse(OwnedPet pet) {
        return OwnedPetResponse.builder()
                .id(pet.getId())
                .name(pet.getName())
                .petType(pet.getPetType().name())
                .breed(pet.getBreed())
                .notes(pet.getNotes())
                .createdAt(pet.getCreatedAt())
                .build();
    }

    private PetSittingReviewResponse mapToReviewResponse(PetSittingReview review) {
        return PetSittingReviewResponse.builder()
                .id(review.getId())
                .bookingId(review.getBooking().getId())
                .reviewer(mapToUserResponse(review.getReviewer()))
                .reviewee(mapToUserResponse(review.getReviewee()))
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("Authentication required");
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
}
