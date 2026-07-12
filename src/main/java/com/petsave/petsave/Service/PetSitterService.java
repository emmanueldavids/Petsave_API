package com.petsave.petsave.Service;

import com.petsave.petsave.Entity.PetSitter;
import com.petsave.petsave.Entity.PetSitterStatus;
import com.petsave.petsave.Entity.PetSittingReview;
import com.petsave.petsave.Entity.SitterPetType;
import com.petsave.petsave.Entity.SitterWallet;
import com.petsave.petsave.Entity.User;
import com.petsave.petsave.Repository.PetSitterRepository;
import com.petsave.petsave.Repository.PetSittingReviewRepository;
import com.petsave.petsave.Repository.SitterWalletRepository;
import com.petsave.petsave.Repository.UserRepository;
import com.petsave.petsave.dto.PetSitterRequest;
import com.petsave.petsave.dto.PetSitterResponse;
import com.petsave.petsave.dto.PetSittingReviewResponse;
import com.petsave.petsave.dto.SitterWalletResponse;
import com.petsave.petsave.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PetSitterService {

    private final PetSitterRepository petSitterRepository;
    private final SitterWalletRepository sitterWalletRepository;
    private final PetSittingReviewRepository petSittingReviewRepository;
    private final UserRepository userRepository;

    public List<PetSitterResponse> listActiveSitters(String city) {
        List<PetSitter> sitters = (city == null || city.isBlank())
                ? petSitterRepository.findByStatus(PetSitterStatus.ACTIVE)
                : petSitterRepository.findByStatusAndCityIgnoreCase(PetSitterStatus.ACTIVE, city);
        return sitters.stream().map(s -> mapToResponse(s, false)).collect(Collectors.toList());
    }

    public PetSitterResponse getSitter(Long id) {
        PetSitter sitter = petSitterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sitter not found with id: " + id));
        User viewer = getCurrentUserOrNull();
        boolean privileged = viewer != null && (viewer.getId().equals(sitter.getUser().getId()) || isAdmin(viewer));

        PetSitterResponse response = mapToResponse(sitter, privileged);
        List<PetSittingReviewResponse> reviews = petSittingReviewRepository.findByRevieweeIdOrderByCreatedAtDesc(sitter.getUser().getId())
                .stream().map(this::mapToReviewResponse).collect(Collectors.toList());
        response.setReviews(reviews);
        return response;
    }

    public PetSitterResponse registerSitter(PetSitterRequest request) {
        User currentUser = getCurrentUser();
        if (petSitterRepository.findByUserId(currentUser.getId()).isPresent()) {
            throw new RuntimeException("You have already registered as a pet sitter");
        }

        PetSitter sitter = new PetSitter();
        sitter.setUser(currentUser);
        applyRequest(sitter, request);
        sitter.setStatus(PetSitterStatus.PENDING);

        PetSitter saved = petSitterRepository.save(sitter);

        if (sitterWalletRepository.findByUserId(currentUser.getId()).isEmpty()) {
            SitterWallet wallet = new SitterWallet();
            wallet.setUser(currentUser);
            sitterWalletRepository.save(wallet);
        }

        return mapToResponse(saved, true);
    }

    public PetSitterResponse updateSitterProfile(Long id, PetSitterRequest request) {
        User currentUser = getCurrentUser();
        PetSitter sitter = petSitterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sitter not found with id: " + id));
        if (!sitter.getUser().getId().equals(currentUser.getId()) && !isAdmin(currentUser)) {
            throw new RuntimeException("Only the sitter can update this profile");
        }

        applyRequest(sitter, request);
        // Bank details changed → the cached Paystack recipient (if any) is stale
        sitter.setPaystackRecipientCode(null);

        PetSitter saved = petSitterRepository.save(sitter);
        return mapToResponse(saved, true);
    }

    public PetSitterResponse updateStatus(Long id, String statusParam) {
        User currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) {
            throw new RuntimeException("Only admins can change sitter status");
        }
        PetSitter sitter = petSitterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sitter not found with id: " + id));

        PetSitterStatus status = parseEnum(PetSitterStatus.class, statusParam, "status");
        sitter.setStatus(status);
        PetSitter saved = petSitterRepository.save(sitter);
        return mapToResponse(saved, true);
    }

    public List<PetSitterResponse> listForAdmin(String statusParam) {
        User currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) {
            throw new RuntimeException("Only admins can view the sitter review queue");
        }

        List<PetSitter> sitters;
        if (statusParam == null || statusParam.isBlank()) {
            sitters = petSitterRepository.findByStatus(PetSitterStatus.PENDING);
        } else if ("ALL".equalsIgnoreCase(statusParam)) {
            sitters = petSitterRepository.findAll();
        } else {
            PetSitterStatus status = parseEnum(PetSitterStatus.class, statusParam, "status");
            sitters = petSitterRepository.findByStatus(status);
        }

        return sitters.stream().map(s -> mapToResponse(s, true)).collect(Collectors.toList());
    }

    public PetSitterResponse getMySitterProfile() {
        User currentUser = getCurrentUser();
        PetSitter sitter = petSitterRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("You have not registered as a pet sitter yet"));
        return mapToResponse(sitter, true);
    }

    public SitterWalletResponse getMyWallet() {
        User currentUser = getCurrentUser();
        SitterWallet wallet = sitterWalletRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("You have not registered as a pet sitter yet"));
        return SitterWalletResponse.builder()
                .balance(wallet.getBalance())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    private void applyRequest(PetSitter sitter, PetSitterRequest request) {
        sitter.setBio(request.getBio());
        sitter.setProfileImageUrl(request.getProfileImageUrl());
        sitter.setServiceRadius(request.getServiceRadius());
        sitter.setLocation(request.getLocation());
        sitter.setCity(request.getCity());
        sitter.setAcceptedPetTypes(request.getAcceptedPetTypes().stream()
                .map(type -> parseEnum(SitterPetType.class, type, "acceptedPetTypes"))
                .collect(Collectors.toSet()));
        sitter.setRatePerDay(request.getRatePerDay());
        sitter.setRatePerNight(request.getRatePerNight());
        sitter.setMaxPetsAtOnce(request.getMaxPetsAtOnce());
        sitter.setBankCode(request.getBankCode());
        sitter.setAccountNumber(request.getAccountNumber());
        sitter.setAccountName(request.getAccountName());
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumType, String value, String fieldName) {
        try {
            return Enum.valueOf(enumType, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid " + fieldName + ": " + value);
        }
    }

    private boolean isAdmin(User user) {
        return "ADMIN".equalsIgnoreCase(user.getRole());
    }

    private PetSitterResponse mapToResponse(PetSitter sitter, boolean includePrivateFields) {
        return PetSitterResponse.builder()
                .id(sitter.getId())
                .user(mapToUserResponse(sitter.getUser()))
                .bio(sitter.getBio())
                .profileImageUrl(sitter.getProfileImageUrl())
                .serviceRadius(sitter.getServiceRadius())
                .location(sitter.getLocation())
                .city(sitter.getCity())
                .acceptedPetTypes(sitter.getAcceptedPetTypes().stream().map(Enum::name).collect(Collectors.toSet()))
                .ratePerDay(sitter.getRatePerDay())
                .ratePerNight(sitter.getRatePerNight())
                .maxPetsAtOnce(sitter.getMaxPetsAtOnce())
                .status(sitter.getStatus().name())
                .averageRating(sitter.getAverageRating())
                .totalReviews(sitter.getTotalReviews())
                .bankCode(includePrivateFields ? sitter.getBankCode() : null)
                .accountNumber(includePrivateFields ? sitter.getAccountNumber() : null)
                .accountName(includePrivateFields ? sitter.getAccountName() : null)
                .payoutDetailsOnFile(includePrivateFields ? (sitter.getBankCode() != null && sitter.getAccountNumber() != null) : null)
                .createdAt(sitter.getCreatedAt())
                .updatedAt(sitter.getUpdatedAt())
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
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

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("Authentication required");
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    private User getCurrentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }
}
