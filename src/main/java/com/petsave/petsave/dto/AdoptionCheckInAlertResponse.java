package com.petsave.petsave.dto;

import com.petsave.petsave.Entity.AdoptionCheckInMilestone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdoptionCheckInAlertResponse {
    private Long checkInId;
    private Long adoptionId;
    private String adopterName;
    private String adopterEmail;
    private String petName;
    private AdoptionCheckInMilestone milestone;
    private LocalDateTime dueDate;
    private LocalDateTime overdueSinceDate;
    private long hoursOverdue;
}
