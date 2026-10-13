package com.petsave.petsave.Service;

import com.petsave.petsave.Entity.FosterApplication;
import com.petsave.petsave.Entity.FosterApplicationStatus;
import com.petsave.petsave.Entity.Notification;
import com.petsave.petsave.Entity.Pet;
import com.petsave.petsave.Entity.PetStatus;
import com.petsave.petsave.Entity.User;
import com.petsave.petsave.Repository.FosterApplicationRepository;
import com.petsave.petsave.Repository.PetRepository;
import com.petsave.petsave.Repository.UserRepository;
import com.petsave.petsave.dto.FosterApplicationRequest;
import com.petsave.petsave.dto.FosterApplicationResponse;
import com.petsave.petsave.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FosterApplicationService {

    private static final List<FosterApplicationStatus> OPEN_STATUSES =
            List.of(FosterApplicationStatus.PENDING, FosterApplicationStatus.APPROVED, FosterApplicationStatus.ACTIVE);

    private final FosterApplicationRepository fosterApplicationRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Value("${app.admin.email:admin@petsave.com}")
    private String adminEmail;

    public FosterApplicationResponse applyToFoster(FosterApplicationRequest request) {
        User applicant = getCurrentUser();
        Pet pet = petRepository.findById(request.getPetId())
                .orElseThrow(() -> new RuntimeException("Pet not found with id: " + request.getPetId()));

        if (fosterApplicationRepository.existsByPetIdAndApplicantIdAndStatusIn(pet.getId(), applicant.getId(), OPEN_STATUSES)) {
            throw new RuntimeException("You already have an open foster application for this pet");
        }

        FosterApplication application = new FosterApplication();
        application.setApplicant(applicant);
        application.setPet(pet);
        application.setStartDate(request.getStartDate());
        application.setExpectedEndDate(request.getExpectedEndDate());
        application.setApplicationReason(request.getApplicationReason());
        application.setHomeDescription(request.getHomeDescription());
        application.setHasExperience(request.getHasExperience());
        application.setExperienceDetails(request.getExperienceDetails());
        application.setStatus(FosterApplicationStatus.PENDING);

        FosterApplication saved = fosterApplicationRepository.save(application);

        notificationService.notify(adminEmail, Notification.NotificationType.FOSTER_APPLICATION_RECEIVED,
                applicant.getName() + " applied to foster " + pet.getName());

        return mapToResponse(saved);
    }

    public List<FosterApplicationResponse> listMyApplications() {
        User currentUser = getCurrentUser();
        return fosterApplicationRepository.findByApplicantIdOrderByCreatedAtDesc(currentUser.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<FosterApplicationResponse> listForAdmin(String statusParam) {
        User currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) {
            throw new RuntimeException("Only admins can view foster applications");
        }

        List<FosterApplication> applications;
        if (statusParam == null || statusParam.isBlank()) {
            applications = fosterApplicationRepository.findByStatusOrderByCreatedAtDesc(FosterApplicationStatus.PENDING);
        } else if ("ALL".equalsIgnoreCase(statusParam)) {
            applications = fosterApplicationRepository.findAllByOrderByCreatedAtDesc();
        } else {
            applications = fosterApplicationRepository.findByStatusOrderByCreatedAtDesc(parseStatus(statusParam));
        }
        return applications.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public FosterApplicationResponse updateStatus(Long id, String statusParam, String adminNotes) {
        User currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) {
            throw new RuntimeException("Only admins can change a foster application's status");
        }
        FosterApplicationStatus status = parseStatus(statusParam);
        if (status == FosterApplicationStatus.COMPLETED) {
            throw new RuntimeException("Use the complete endpoint to mark a foster period complete");
        }

        FosterApplication application = fosterApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Foster application not found with id: " + id));
        if (application.getStatus() == FosterApplicationStatus.COMPLETED) {
            throw new RuntimeException("A completed foster application cannot change status");
        }

        application.setStatus(status);
        if (adminNotes != null && !adminNotes.isBlank()) {
            application.setAdminNotes(adminNotes);
        }

        if (status == FosterApplicationStatus.APPROVED || status == FosterApplicationStatus.ACTIVE) {
            Pet pet = application.getPet();
            pet.setStatus(PetStatus.FOSTER_CARE);
            petRepository.save(pet);
        }

        FosterApplication saved = fosterApplicationRepository.save(application);

        notificationService.notify(application.getApplicant().getEmail(), Notification.NotificationType.FOSTER_STATUS_UPDATE,
                "Your foster application for " + application.getPet().getName() + " is now: " + status.name());

        return mapToResponse(saved);
    }

    public FosterApplicationResponse completeFoster(Long id) {
        User currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) {
            throw new RuntimeException("Only admins can mark a foster period complete");
        }
        FosterApplication application = fosterApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Foster application not found with id: " + id));
        if (application.getStatus() == FosterApplicationStatus.COMPLETED || application.getStatus() == FosterApplicationStatus.REJECTED) {
            throw new RuntimeException("A foster application that is " + application.getStatus() + " cannot be completed");
        }

        application.setStatus(FosterApplicationStatus.COMPLETED);

        Pet pet = application.getPet();
        if (pet.getStatus() == PetStatus.FOSTER_CARE) {
            pet.setStatus(PetStatus.FOR_ADOPTION);
            petRepository.save(pet);
        }

        FosterApplication saved = fosterApplicationRepository.save(application);

        notificationService.notify(application.getApplicant().getEmail(), Notification.NotificationType.FOSTER_COMPLETED,
                "Your foster period for " + application.getPet().getName() + " has been marked complete. Thank you!");

        return mapToResponse(saved);
    }

    private FosterApplicationStatus parseStatus(String value) {
        try {
            return FosterApplicationStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + value);
        }
    }

    private boolean isAdmin(User user) {
        return "ADMIN".equalsIgnoreCase(user.getRole());
    }

    private FosterApplicationResponse mapToResponse(FosterApplication application) {
        Pet pet = application.getPet();
        return FosterApplicationResponse.builder()
                .id(application.getId())
                .applicant(mapToUserResponse(application.getApplicant()))
                .pet(FosterApplicationResponse.PetSummary.builder()
                        .id(pet.getId())
                        .name(pet.getName())
                        .type(pet.getType() != null ? pet.getType().name() : null)
                        .breed(pet.getBreed())
                        .imageUrl(pet.getImageUrl())
                        .status(pet.getStatus() != null ? pet.getStatus().name() : null)
                        .build())
                .startDate(application.getStartDate())
                .expectedEndDate(application.getExpectedEndDate())
                .applicationReason(application.getApplicationReason())
                .homeDescription(application.getHomeDescription())
                .hasExperience(application.getHasExperience())
                .experienceDetails(application.getExperienceDetails())
                .status(application.getStatus().name())
                .adminNotes(application.getAdminNotes())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
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
