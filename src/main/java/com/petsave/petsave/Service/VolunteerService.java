package com.petsave.petsave.Service;

import com.petsave.petsave.Entity.*;
import com.petsave.petsave.Repository.PetRepository;
import com.petsave.petsave.Repository.UserRepository;
import com.petsave.petsave.Repository.VolunteerRepository;
import com.petsave.petsave.Repository.VolunteerTaskRepository;
import com.petsave.petsave.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class VolunteerService {

    private static final List<VolunteerStatus> PUBLIC_STATUSES = List.of(VolunteerStatus.APPROVED, VolunteerStatus.ACTIVE);

    private final VolunteerRepository volunteerRepository;
    private final VolunteerTaskRepository volunteerTaskRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;

    public List<VolunteerResponse> listApprovedVolunteers() {
        return volunteerRepository.findByStatusIn(PUBLIC_STATUSES).stream()
                .map(v -> mapToResponse(v, false))
                .collect(Collectors.toList());
    }

    public List<VolunteerResponse> listForAdmin(String statusParam) {
        User currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) {
            throw new RuntimeException("Only admins can view the volunteer review queue");
        }

        List<Volunteer> volunteers;
        if (statusParam == null || statusParam.isBlank()) {
            volunteers = volunteerRepository.findByStatusOrderByCreatedAtDesc(VolunteerStatus.PENDING);
        } else if ("ALL".equalsIgnoreCase(statusParam)) {
            volunteers = volunteerRepository.findAllByOrderByCreatedAtDesc();
        } else {
            VolunteerStatus status = parseEnum(VolunteerStatus.class, statusParam, "status");
            volunteers = volunteerRepository.findByStatusOrderByCreatedAtDesc(status);
        }

        return volunteers.stream().map(v -> mapToResponse(v, true)).collect(Collectors.toList());
    }

    public VolunteerResponse getVolunteer(Long id) {
        Volunteer volunteer = volunteerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Volunteer not found with id: " + id));
        User viewer = getCurrentUserOrNull();
        boolean privileged = viewer != null && (viewer.getId().equals(volunteer.getUser().getId()) || isAdmin(viewer));
        return mapToResponse(volunteer, privileged);
    }

    public VolunteerResponse registerVolunteer(VolunteerRequest request) {
        User currentUser = getCurrentUser();
        if (volunteerRepository.findByUserId(currentUser.getId()).isPresent()) {
            throw new RuntimeException("You have already registered as a volunteer");
        }

        Volunteer volunteer = new Volunteer();
        volunteer.setUser(currentUser);
        applyRequest(volunteer, request);
        volunteer.setStatus(VolunteerStatus.PENDING);

        Volunteer saved = volunteerRepository.save(volunteer);
        return mapToResponse(saved, true);
    }

    public VolunteerResponse updateVolunteerProfile(Long id, VolunteerRequest request) {
        User currentUser = getCurrentUser();
        Volunteer volunteer = volunteerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Volunteer not found with id: " + id));
        if (!volunteer.getUser().getId().equals(currentUser.getId()) && !isAdmin(currentUser)) {
            throw new RuntimeException("Only the volunteer can update this profile");
        }

        applyRequest(volunteer, request);
        Volunteer saved = volunteerRepository.save(volunteer);
        return mapToResponse(saved, true);
    }

    public VolunteerResponse updateStatus(Long id, String statusParam) {
        User currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) {
            throw new RuntimeException("Only admins can change volunteer status");
        }
        Volunteer volunteer = volunteerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Volunteer not found with id: " + id));

        VolunteerStatus status = parseEnum(VolunteerStatus.class, statusParam, "status");
        volunteer.setStatus(status);
        Volunteer saved = volunteerRepository.save(volunteer);
        return mapToResponse(saved, true);
    }

    public VolunteerResponse getMyVolunteerProfile() {
        User currentUser = getCurrentUser();
        Volunteer volunteer = volunteerRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("You have not registered as a volunteer yet"));
        return mapToResponse(volunteer, true);
    }

    public List<VolunteerTaskResponse> listTasksForVolunteer(Long volunteerId) {
        User currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) {
            throw new RuntimeException("Only admins can view a volunteer's task history");
        }
        Volunteer volunteer = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new RuntimeException("Volunteer not found with id: " + volunteerId));
        return volunteerTaskRepository.findByVolunteerOrderByCreatedAtDesc(volunteer).stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    public List<VolunteerTaskResponse> listMyTasks() {
        User currentUser = getCurrentUser();
        Volunteer volunteer = volunteerRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("You have not registered as a volunteer yet"));
        return volunteerTaskRepository.findByVolunteerOrderByCreatedAtDesc(volunteer).stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    public VolunteerTaskResponse createTask(VolunteerTaskRequest request) {
        User currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) {
            throw new RuntimeException("Only admins can create and assign tasks");
        }

        Volunteer volunteer = volunteerRepository.findById(request.getVolunteerId())
                .orElseThrow(() -> new RuntimeException("Volunteer not found with id: " + request.getVolunteerId()));

        VolunteerTask task = new VolunteerTask();
        task.setVolunteer(volunteer);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setTaskType(parseEnum(VolunteerTaskType.class, request.getTaskType(), "taskType"));
        task.setScheduledDate(request.getScheduledDate());
        task.setAdminNotes(request.getAdminNotes());
        task.setStatus(VolunteerTaskStatus.ASSIGNED);

        if (request.getPetId() != null) {
            Pet pet = petRepository.findById(request.getPetId())
                    .orElseThrow(() -> new RuntimeException("Pet not found with id: " + request.getPetId()));
            task.setPet(pet);
        }

        VolunteerTask saved = volunteerTaskRepository.save(task);
        return mapToTaskResponse(saved);
    }

    public VolunteerTaskResponse completeTask(Long taskId, Integer hoursSpent) {
        User currentUser = getCurrentUser();
        VolunteerTask task = volunteerTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        Volunteer volunteer = task.getVolunteer();
        if (!volunteer.getUser().getId().equals(currentUser.getId()) && !isAdmin(currentUser)) {
            throw new RuntimeException("Only the assigned volunteer can complete this task");
        }

        task.setStatus(VolunteerTaskStatus.COMPLETED);
        task.setCompletedDate(LocalDateTime.now());
        task.setHoursSpent(hoursSpent);
        VolunteerTask saved = volunteerTaskRepository.save(task);

        volunteer.setHoursLogged(volunteer.getHoursLogged() + hoursSpent);
        volunteer.setTotalTasksCompleted(volunteer.getTotalTasksCompleted() + 1);
        volunteer.setBadgeLevel(calculateBadgeLevel(volunteer.getTotalTasksCompleted()));
        volunteerRepository.save(volunteer);

        return mapToTaskResponse(saved);
    }

    public VolunteerStatsResponse getStats() {
        long totalVolunteers = volunteerRepository.countByStatusIn(PUBLIC_STATUSES);
        long totalHoursLogged = volunteerRepository.sumHoursLogged();
        long totalTasksCompleted = volunteerRepository.sumTasksCompleted();

        Map<String, Long> byBadge = new LinkedHashMap<>();
        for (BadgeLevel level : BadgeLevel.values()) {
            byBadge.put(level.name(), volunteerRepository.countByBadgeLevel(level));
        }

        return VolunteerStatsResponse.builder()
                .totalVolunteers(totalVolunteers)
                .totalHoursLogged(totalHoursLogged)
                .totalTasksCompleted(totalTasksCompleted)
                .volunteersByBadgeLevel(byBadge)
                .build();
    }

    private BadgeLevel calculateBadgeLevel(int totalTasksCompleted) {
        if (totalTasksCompleted >= 50) return BadgeLevel.PLATINUM;
        if (totalTasksCompleted >= 30) return BadgeLevel.GOLD;
        if (totalTasksCompleted >= 15) return BadgeLevel.SILVER;
        if (totalTasksCompleted >= 5) return BadgeLevel.BRONZE;
        return BadgeLevel.NONE;
    }

    private void applyRequest(Volunteer volunteer, VolunteerRequest request) {
        volunteer.setSkills(request.getSkills().stream()
                .map(skill -> parseEnum(VolunteerSkill.class, skill, "skill"))
                .collect(Collectors.toSet()));
        volunteer.setAvailability(parseEnum(VolunteerAvailability.class, request.getAvailability(), "availability"));
        volunteer.setLocation(request.getLocation());
        volunteer.setBio(request.getBio());
        volunteer.setEmergencyContact(request.getEmergencyContact());
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

    private VolunteerResponse mapToResponse(Volunteer volunteer, boolean includePrivateFields) {
        return VolunteerResponse.builder()
                .id(volunteer.getId())
                .user(mapToUserResponse(volunteer.getUser()))
                .skills(volunteer.getSkills().stream().map(Enum::name).collect(Collectors.toSet()))
                .availability(volunteer.getAvailability() != null ? volunteer.getAvailability().name() : null)
                .location(volunteer.getLocation())
                .bio(volunteer.getBio())
                .emergencyContact(includePrivateFields ? volunteer.getEmergencyContact() : null)
                .status(volunteer.getStatus().name())
                .hoursLogged(volunteer.getHoursLogged())
                .totalTasksCompleted(volunteer.getTotalTasksCompleted())
                .badgeLevel(volunteer.getBadgeLevel().name())
                .createdAt(volunteer.getCreatedAt())
                .updatedAt(volunteer.getUpdatedAt())
                .build();
    }

    private VolunteerTaskResponse mapToTaskResponse(VolunteerTask task) {
        Pet pet = task.getPet();
        return VolunteerTaskResponse.builder()
                .id(task.getId())
                .volunteerId(task.getVolunteer().getId())
                .volunteerUser(mapToUserResponse(task.getVolunteer().getUser()))
                .pet(pet != null ? VolunteerTaskResponse.PetSummary.builder()
                        .id(pet.getId())
                        .name(pet.getName())
                        .imageUrl(pet.getImageUrl())
                        .build() : null)
                .title(task.getTitle())
                .description(task.getDescription())
                .taskType(task.getTaskType().name())
                .status(task.getStatus().name())
                .scheduledDate(task.getScheduledDate())
                .completedDate(task.getCompletedDate())
                .hoursSpent(task.getHoursSpent())
                .adminNotes(task.getAdminNotes())
                .createdAt(task.getCreatedAt())
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

    private User getCurrentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }
}
