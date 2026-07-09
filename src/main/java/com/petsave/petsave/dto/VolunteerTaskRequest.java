package com.petsave.petsave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolunteerTaskRequest {

    @NotNull(message = "volunteerId is required")
    private Long volunteerId;

    private Long petId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "taskType is required")
    private String taskType;

    private LocalDateTime scheduledDate;

    private String adminNotes;
}
