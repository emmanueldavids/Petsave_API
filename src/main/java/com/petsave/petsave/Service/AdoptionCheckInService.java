package com.petsave.petsave.Service;

import com.petsave.petsave.Entity.*;
import com.petsave.petsave.Repository.AdoptionCheckInRepository;
import com.petsave.petsave.Repository.AdoptionRepository;
import com.petsave.petsave.Utils.EmailUtil;
import com.petsave.petsave.dto.AdoptionCheckInAlertResponse;
import com.petsave.petsave.dto.AdoptionCheckInResponse;
import com.petsave.petsave.dto.AdoptionCheckInSubmitRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdoptionCheckInService {

    private final AdoptionCheckInRepository checkInRepository;
    private final AdoptionRepository adoptionRepository;
    private final EmailUtil emailUtil;
    private final CloudinaryService cloudinaryService;
    private final NotificationService notificationService;

    @Value("${app.upload.check-in-photos:}")
    private String uploadDir;

    @Value("${app.admin.email:admin@petsave.com}")
    private String adminEmail;

    // ================= CREATE CHECK-INS FOR NEW COMPLETED ADOPTIONS =================
    @Async
    public void createCheckInsForAdoption(Long adoptionId) {
        Adoption adoption = adoptionRepository.findById(adoptionId)
                .orElseThrow(() -> new RuntimeException("Adoption not found"));

        // Allow creation when adoption is APPROVED or COMPLETED
        if (adoption.getStatus() != AdoptionStatus.COMPLETED && adoption.getStatus() != AdoptionStatus.APPROVED) {
            log.warn("Adoption {} is not in APPROVED/COMPLETED status. Skipping check-in creation.", adoptionId);
            return;
        }

        // Avoid creating duplicate check-ins
        var existing = checkInRepository.findByAdoptionId(adoptionId);
        if (existing != null && !existing.isEmpty()) {
            log.info("Check-ins already exist for adoption {}. Skipping creation.", adoptionId);
            return;
        }

        LocalDateTime adoptionDate = adoption.getCreatedAt();

        // Create check-ins for all milestones
        for (AdoptionCheckInMilestone milestone : AdoptionCheckInMilestone.values()) {
            LocalDateTime dueDate = adoptionDate.plusDays(milestone.getDaysAfterAdoption());

            AdoptionCheckIn checkIn = AdoptionCheckIn.builder()
                    .adoption(adoption)
                    .milestone(milestone)
                    .status(AdoptionCheckInStatus.PENDING)
                    .healthStatus(AdoptionCheckInHealthStatus.NOT_SUBMITTED)
                    .dueDate(dueDate)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            checkInRepository.save(checkIn);
            log.info("Created check-in for adoption {} at milestone {}", adoptionId, milestone);
        }
    }

    // ================= SUBMIT CHECK-IN (WITH PHOTO UPLOAD) =================
    public AdoptionCheckInResponse submitCheckIn(Long adoptionId, Long checkInId, 
                                                  AdoptionCheckInSubmitRequest request,
                                                  MultipartFile photoFile) throws IOException {
        
        AdoptionCheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> new RuntimeException("Check-in not found"));

        if (!checkIn.getAdoption().getId().equals(adoptionId)) {
            throw new RuntimeException("Check-in does not belong to this adoption");
        }

        if (checkIn.getStatus() == AdoptionCheckInStatus.SUBMITTED) {
            throw new RuntimeException("This check-in has already been submitted");
        }

        // Upload photo if provided
        String photoUrl = null;
        if (photoFile != null && !photoFile.isEmpty()) {
            photoUrl = uploadCheckInPhoto(photoFile);
        }

        // Update check-in
        checkIn.setStatus(AdoptionCheckInStatus.SUBMITTED);
        checkIn.setSubmittedAt(LocalDateTime.now());
        checkIn.setAdopterNotes(request.getAdopterNotes());
        checkIn.setPhotoUrl(photoUrl);
        
        try {
            AdoptionCheckInHealthStatus healthStatus = AdoptionCheckInHealthStatus.valueOf(request.getHealthStatus());
            checkIn.setHealthStatus(healthStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid health status: " + request.getHealthStatus());
        }

        checkInRepository.save(checkIn);

        // Send confirmation email to adopter
        emailUtil.sendCheckInConfirmationEmail(
                checkIn.getAdoption().getAdopterEmail(),
                checkIn.getAdoption().getAdopterName(),
                checkIn.getMilestone().name()
        );

        // Notify admin if health concerns
        if (checkIn.getHealthStatus() == AdoptionCheckInHealthStatus.NEEDS_VET ||
            checkIn.getHealthStatus() == AdoptionCheckInHealthStatus.CONCERNS) {
            emailUtil.sendAdminHealthConcernAlert(
                    checkIn.getAdoption().getPetName(),
                    checkIn.getAdoption().getAdopterName(),
                    checkIn.getHealthStatus().name(),
                    checkIn.getAdopterNotes()
            );
            notificationService.notify(adminEmail, Notification.NotificationType.CHECKIN_HEALTH_CONCERN,
                    "Health concern (" + checkIn.getHealthStatus().name() + ") reported for " + checkIn.getAdoption().getPetName()
                            + " by " + checkIn.getAdoption().getAdopterName());
        }

        return mapToResponse(checkIn);
    }

    // ================= GET ADOPTER'S CHECK-INS =================
    public List<AdoptionCheckInResponse> getCheckInsByAdoption(Long adoptionId) {
        List<AdoptionCheckIn> checkIns = checkInRepository.findByAdoptionIdOrderByDueDateAsc(adoptionId);
        return checkIns.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ================= GET SINGLE CHECK-IN =================
    public AdoptionCheckInResponse getCheckInById(Long checkInId) {
        AdoptionCheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> new RuntimeException("Check-in not found"));
        return mapToResponse(checkIn);
    }

    // ================= SCHEDULER: CREATE CHECK-INS FOR NEWLY COMPLETED ADOPTIONS =================
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void scheduleCheckInCreation() {
        log.info("Running scheduled check-in creation task");
        
        List<Adoption> completedAdoptions = adoptionRepository.findByStatus(AdoptionStatus.COMPLETED);
        
        for (Adoption adoption : completedAdoptions) {
            // Check if check-ins already exist for this adoption
            List<AdoptionCheckIn> existingCheckIns = checkInRepository.findByAdoptionId(adoption.getId());
            if (existingCheckIns.isEmpty()) {
                createCheckInsForAdoption(adoption.getId());
            }
        }
    }

    // ================= SCHEDULER: SEND EMAIL REMINDERS FOR UPCOMING DUE CHECK-INS =================
    @Scheduled(cron = "0 9 * * * *") // 9 AM daily
    public void scheduleCheckInReminders() {
        log.info("Running scheduled check-in reminders task");
        
        // Find check-ins due within next 24 hours
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        
        List<AdoptionCheckIn> upcomingCheckIns = checkInRepository.findPendingCheckInsDueBy(tomorrow)
                .stream()
                .filter(c -> c.getStatus() == AdoptionCheckInStatus.PENDING && 
                           c.getDueDate().isAfter(now))
                .collect(Collectors.toList());

        for (AdoptionCheckIn checkIn : upcomingCheckIns) {
            emailUtil.sendCheckInReminderEmail(
                    checkIn.getAdoption().getAdopterEmail(),
                    checkIn.getAdoption().getAdopterName(),
                    checkIn.getAdoption().getPetName(),
                    checkIn.getMilestone().name(),
                    checkIn.getDueDate()
            );
            if (checkIn.getAdoption().getUser() != null) {
                notificationService.notify(checkIn.getAdoption().getUser().getEmail(), Notification.NotificationType.CHECKIN_REMINDER,
                        "A " + checkIn.getMilestone().name() + " check-in for " + checkIn.getAdoption().getPetName() + " is due soon");
            }
            log.info("Sent reminder for check-in {} to {}", checkIn.getId(), checkIn.getAdoption().getAdopterEmail());
        }
    }

    // ================= SCHEDULER: ADMIN ALERTS FOR MISSED CHECK-INS (72 HOURS OVERDUE) =================
    @Scheduled(cron = "0 10 * * * *") // 10 AM daily
    public void scheduleAdminAlerts() {
        log.info("Running scheduled admin alerts task for missed check-ins");
        
        LocalDateTime alertCutoff = LocalDateTime.now().minus(72, ChronoUnit.HOURS);
        List<AdoptionCheckIn> overdueCheckIns = checkInRepository.findCheckInsForAdminAlert(alertCutoff);

        for (AdoptionCheckIn checkIn : overdueCheckIns) {
            checkIn.setStatus(AdoptionCheckInStatus.OVERDUE);
            checkInRepository.save(checkIn);

            // Send alert to admin
            emailUtil.sendAdminMissedCheckInAlert(
                    checkIn.getAdoption().getAdopterName(),
                    checkIn.getAdoption().getAdopterEmail(),
                    checkIn.getAdoption().getPetName(),
                    checkIn.getMilestone().name(),
                    checkIn.getDueDate()
            );
            notificationService.notify(adminEmail, Notification.NotificationType.CHECKIN_OVERDUE,
                    checkIn.getAdoption().getAdopterName() + " missed the " + checkIn.getMilestone().name()
                            + " check-in for " + checkIn.getAdoption().getPetName());
            log.warn("Admin alert sent for overdue check-in {} (adoption {})", checkIn.getId(), checkIn.getAdoption().getId());
        }
    }

    // ================= GET ADMIN ALERT LIST =================
    public List<AdoptionCheckInAlertResponse> getAdminAlerts() {
        LocalDateTime alertCutoff = LocalDateTime.now().minus(72, ChronoUnit.HOURS);
        List<AdoptionCheckIn> overdueCheckIns = checkInRepository.findCheckInsForAdminAlert(alertCutoff);

        return overdueCheckIns.stream()
                .map(checkIn -> AdoptionCheckInAlertResponse.builder()
                        .checkInId(checkIn.getId())
                        .adoptionId(checkIn.getAdoption().getId())
                        .adopterName(checkIn.getAdoption().getAdopterName())
                        .adopterEmail(checkIn.getAdoption().getAdopterEmail())
                        .petName(checkIn.getAdoption().getPetName())
                        .milestone(checkIn.getMilestone())
                        .dueDate(checkIn.getDueDate())
                        .overdueSinceDate(checkIn.getDueDate())
                        .hoursOverdue(ChronoUnit.HOURS.between(checkIn.getDueDate(), LocalDateTime.now()))
                        .build())
                .collect(Collectors.toList());
    }

    // ================= PHOTO UPLOAD HELPER =================
    private String uploadCheckInPhoto(MultipartFile file) throws IOException {
        // Option 1: Use Cloudinary (preferred for production)
        return cloudinaryService.uploadFile(file, "adoption-check-ins");
        
        // Option 2: Local file storage (if Cloudinary not available)
        // return uploadPhotoLocally(file);
    }

    private String uploadPhotoLocally(MultipartFile file) throws IOException {
        if (uploadDir == null || uploadDir.isBlank()) {
            throw new RuntimeException("Local upload directory is not configured");
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
        
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, file.getBytes());
        
        return "/api/files/check-in-photos/" + fileName;
    }

    // ================= UTILITY: MAP TO RESPONSE DTO =================
    private AdoptionCheckInResponse mapToResponse(AdoptionCheckIn checkIn) {
        LocalDateTime now = LocalDateTime.now();
        boolean isOverdue = checkIn.getStatus() == AdoptionCheckInStatus.PENDING && checkIn.getDueDate().isBefore(now);
        long hoursUntilDue = ChronoUnit.HOURS.between(now, checkIn.getDueDate());

        return AdoptionCheckInResponse.builder()
                .id(checkIn.getId())
                .adoptionId(checkIn.getAdoption().getId())
                .petName(checkIn.getAdoption().getPetName())
                .milestone(checkIn.getMilestone())
                .status(checkIn.getStatus())
                .healthStatus(checkIn.getHealthStatus())
                .dueDate(checkIn.getDueDate())
                .submittedAt(checkIn.getSubmittedAt())
                .adopterNotes(checkIn.getAdopterNotes())
                .photoUrl(checkIn.getPhotoUrl())
                .createdAt(checkIn.getCreatedAt())
                .updatedAt(checkIn.getUpdatedAt())
                .isOverdue(isOverdue)
                .hoursUntilDue(hoursUntilDue)
                .build();
    }

    // ================= GET STATISTICS =================
    public long getCompletedCheckInsCount(Long adoptionId) {
        return checkInRepository.countByAdoptionIdAndStatus(adoptionId, AdoptionCheckInStatus.SUBMITTED);
    }

    public long getPendingCheckInsCount(Long adoptionId) {
        return checkInRepository.countByAdoptionIdAndStatus(adoptionId, AdoptionCheckInStatus.PENDING);
    }
}
