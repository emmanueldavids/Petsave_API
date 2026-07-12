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

import java.math.BigDecimal;
import java.math.RoundingMode;
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

        long days = ChronoUnit.DAYS.between(request.getStartDate().toLocalDate(), request.getEndDate().toLocalDate());
        if (days < 1) {
            days = 1;
        }
        BigDecimal totalAmount = sitterProfile.getRatePerDay().multiply(BigDecimal.valueOf(days));

        String reference = "PETSIT_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        PetSitting booking = new PetSitting();
        booking.setOwner(owner);
        booking.setSitter(sitterProfile.getUser());
        booking.setPet(pet);
        booking.setStartDate(request.getStartDate());
        booking.setEndDate(request.getEndDate());
        booking.setTotalAmount(totalAmount);
        booking.setStatus(PetSittingStatus.REQUESTED);
        booking.setPaymentReference(reference);
        booking.setPaymentStatus(PetSittingPaymentStatus.PENDING);
        booking.setOwnerNotes(request.getOwnerNotes());

        PetSitting saved = petSittingRepository.save(booking);

        String authorizationUrl = initializePaystackPayment(owner, totalAmount, reference, request.getCallbackUrl());

        PetSittingResponse response = mapToResponse(saved);
        response.setPaymentAuthorizationUrl(authorizationUrl);
        return response;
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
        if (booking.getStatus() != PetSittingStatus.REQUESTED && booking.getStatus() != PetSittingStatus.CONFIRMED) {
            throw new RuntimeException("A booking that is " + booking.getStatus() + " can no longer be cancelled");
        }

        booking.setStatus(PetSittingStatus.CANCELLED);
        // Only PENDING payments are simply void. A PAID booking is NOT auto-refunded here —
        // there is no Paystack refund call wired up, so paymentStatus intentionally stays PAID
        // as a signal that a manual refund is owed. Do not silently mark it REFUNDED.
        PetSitting saved = petSittingRepository.save(booking);

        User other = booking.getOwner().getId().equals(currentUser.getId()) ? booking.getSitter() : booking.getOwner();
        notifyParticipant(other, "BOOKING_CANCELLED", "A pet sitting booking was cancelled.", booking);
        return mapToResponse(saved);
    }

    public PetSittingResponse adminCompleteBooking(Long id) {
        User currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) {
            throw new RuntimeException("Only admins can force-complete a booking");
        }
        PetSitting booking = petSittingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        if (booking.getStatus() == PetSittingStatus.COMPLETED || booking.getStatus() == PetSittingStatus.CANCELLED) {
            throw new RuntimeException("A booking that is " + booking.getStatus() + " cannot be completed again");
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

        petSittingRepository.save(booking);
        notifyParticipant(booking.getSitter(), "BOOKING_COMPLETED", "A booking was marked completed.", booking);
        notifyParticipant(booking.getOwner(), "BOOKING_COMPLETED", "Your pet sitting booking was marked completed.", booking);
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
                    .block();

            if (response != null && Boolean.TRUE.equals(response.get("status"))) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                return (String) data.get("authorization_url");
            }
            log.error("Paystack initialize failed for reference {}: {}", reference, response);
            throw new RuntimeException("Failed to initialize payment with Paystack");
        } catch (Exception e) {
            log.error("Paystack initialize error for reference {}: {}", reference, e.getMessage(), e);
            throw new RuntimeException("Failed to initialize payment: " + e.getMessage());
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
