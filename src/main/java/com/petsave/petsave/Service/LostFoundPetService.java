package com.petsave.petsave.Service;

import com.petsave.petsave.Entity.LostFoundPet;
import com.petsave.petsave.Entity.LostFoundReportType;
import com.petsave.petsave.Entity.LostFoundStatus;
import com.petsave.petsave.Entity.Notification;
import com.petsave.petsave.Entity.User;
import com.petsave.petsave.Repository.LostFoundPetRepository;
import com.petsave.petsave.Repository.UserRepository;
import com.petsave.petsave.dto.LostFoundPetRequest;
import com.petsave.petsave.dto.LostFoundPetResponse;
import com.petsave.petsave.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LostFoundPetService {

    private final LostFoundPetRepository lostFoundPetRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public List<LostFoundPetResponse> listActive(String city, String reportTypeParam) {
        LostFoundReportType reportType = reportTypeParam != null && !reportTypeParam.isBlank()
                ? parseReportType(reportTypeParam) : null;
        boolean hasCity = city != null && !city.isBlank();

        List<LostFoundPet> reports;
        if (hasCity && reportType != null) {
            reports = lostFoundPetRepository.findByStatusAndCityIgnoreCaseAndReportTypeOrderByCreatedAtDesc(
                    LostFoundStatus.ACTIVE, city, reportType);
        } else if (hasCity) {
            reports = lostFoundPetRepository.findByStatusAndCityIgnoreCaseOrderByCreatedAtDesc(LostFoundStatus.ACTIVE, city);
        } else if (reportType != null) {
            reports = lostFoundPetRepository.findByStatusAndReportTypeOrderByCreatedAtDesc(LostFoundStatus.ACTIVE, reportType);
        } else {
            reports = lostFoundPetRepository.findByStatusOrderByCreatedAtDesc(LostFoundStatus.ACTIVE);
        }
        return reports.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public LostFoundPetResponse getReport(Long id) {
        LostFoundPet report = lostFoundPetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lost & found report not found with id: " + id));
        return mapToResponse(report);
    }

    public List<LostFoundPetResponse> listMyReports() {
        User currentUser = getCurrentUser();
        return lostFoundPetRepository.findByReporterIdOrderByCreatedAtDesc(currentUser.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public LostFoundPetResponse createReport(LostFoundPetRequest request) {
        User currentUser = getCurrentUser();
        if ((request.getContactPhone() == null || request.getContactPhone().isBlank())
                && (request.getContactEmail() == null || request.getContactEmail().isBlank())) {
            throw new RuntimeException("At least one of contactPhone or contactEmail is required");
        }

        LostFoundPet report = new LostFoundPet();
        report.setReporter(currentUser);
        report.setReportType(parseReportType(request.getReportType()));
        applyRequest(report, request);
        report.setStatus(LostFoundStatus.ACTIVE);

        LostFoundPet saved = lostFoundPetRepository.save(report);
        return mapToResponse(saved);
    }

    public LostFoundPetResponse updateReport(Long id, LostFoundPetRequest request) {
        User currentUser = getCurrentUser();
        LostFoundPet report = lostFoundPetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lost & found report not found with id: " + id));
        if (!report.getReporter().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only update your own report");
        }
        if (report.getStatus() != LostFoundStatus.ACTIVE) {
            throw new RuntimeException("Only an active report can be updated — current status: " + report.getStatus());
        }

        if (request.getReportType() != null && !request.getReportType().isBlank()) {
            report.setReportType(parseReportType(request.getReportType()));
        }
        applyRequest(report, request);

        LostFoundPet saved = lostFoundPetRepository.save(report);
        return mapToResponse(saved);
    }

    public LostFoundPetResponse markReunited(Long id) {
        User currentUser = getCurrentUser();
        LostFoundPet report = lostFoundPetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lost & found report not found with id: " + id));
        if (!report.getReporter().getId().equals(currentUser.getId()) && !isAdmin(currentUser)) {
            throw new RuntimeException("You can only mark your own report as reunited");
        }
        if (report.getStatus() != LostFoundStatus.ACTIVE) {
            throw new RuntimeException("Only an active report can be marked reunited — current status: " + report.getStatus());
        }
        report.setStatus(LostFoundStatus.REUNITED);
        LostFoundPet saved = lostFoundPetRepository.save(report);
        return mapToResponse(saved);
    }

    public void deleteReport(Long id) {
        User currentUser = getCurrentUser();
        LostFoundPet report = lostFoundPetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lost & found report not found with id: " + id));
        if (!report.getReporter().getId().equals(currentUser.getId()) && !isAdmin(currentUser)) {
            throw new RuntimeException("You can only delete your own report");
        }
        lostFoundPetRepository.delete(report);
    }

    /**
     * Hourly sweep: active reports past their 60-day expiry get auto-expired.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void autoExpireReports() {
        LocalDateTime now = LocalDateTime.now();
        List<LostFoundPet> due = lostFoundPetRepository.findByStatusAndExpiresAtBefore(LostFoundStatus.ACTIVE, now);
        for (LostFoundPet report : due) {
            try {
                report.setStatus(LostFoundStatus.EXPIRED);
                lostFoundPetRepository.save(report);
                notificationService.notify(report.getReporter().getEmail(), Notification.NotificationType.LOST_FOUND_EXPIRED,
                        "Your " + report.getReportType().name().toLowerCase() + " pet report has expired after 60 days");
            } catch (Exception e) {
                log.error("Auto-expire failed for lost & found report {}: {}", report.getId(), e.getMessage(), e);
            }
        }
    }

    private void applyRequest(LostFoundPet report, LostFoundPetRequest request) {
        report.setPetName(request.getPetName());
        report.setPetType(request.getPetType());
        report.setPetBreed(request.getPetBreed());
        report.setDescription(request.getDescription());
        report.setImageUrl(request.getImageUrl());
        report.setLastSeenLocation(request.getLastSeenLocation());
        report.setCity(request.getCity());
        report.setLatitude(request.getLatitude());
        report.setLongitude(request.getLongitude());
        report.setContactPhone(request.getContactPhone());
        report.setContactEmail(request.getContactEmail());
    }

    private LostFoundReportType parseReportType(String value) {
        try {
            return LostFoundReportType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid reportType: " + value + " (expected LOST or FOUND)");
        }
    }

    private boolean isAdmin(User user) {
        return "ADMIN".equalsIgnoreCase(user.getRole());
    }

    private LostFoundPetResponse mapToResponse(LostFoundPet report) {
        return LostFoundPetResponse.builder()
                .id(report.getId())
                .reporter(mapToUserResponse(report.getReporter()))
                .reportType(report.getReportType().name())
                .petName(report.getPetName())
                .petType(report.getPetType())
                .petBreed(report.getPetBreed())
                .description(report.getDescription())
                .imageUrl(report.getImageUrl())
                .lastSeenLocation(report.getLastSeenLocation())
                .city(report.getCity())
                .latitude(report.getLatitude())
                .longitude(report.getLongitude())
                .contactPhone(report.getContactPhone())
                .contactEmail(report.getContactEmail())
                .status(report.getStatus().name())
                .expiresAt(report.getExpiresAt())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
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
