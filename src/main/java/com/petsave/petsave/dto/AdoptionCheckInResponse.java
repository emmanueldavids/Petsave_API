package com.petsave.petsave.dto;

import com.petsave.petsave.Entity.AdoptionCheckInHealthStatus;
import com.petsave.petsave.Entity.AdoptionCheckInMilestone;
import com.petsave.petsave.Entity.AdoptionCheckInStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdoptionCheckInResponse {
    private Long id;
    private Long adoptionId;
    private String petName;
    private AdoptionCheckInMilestone milestone;
    private AdoptionCheckInStatus status;
    private AdoptionCheckInHealthStatus healthStatus;
    private LocalDateTime dueDate;
    private LocalDateTime submittedAt;
    private String adopterNotes;
    private String photoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isOverdue;
    private long hoursUntilDue;
}
