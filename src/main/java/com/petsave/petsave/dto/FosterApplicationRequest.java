package com.petsave.petsave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FosterApplicationRequest {

    @NotNull(message = "petId is required")
    private Long petId;

    @NotNull(message = "startDate is required")
    private LocalDate startDate;

    @NotNull(message = "expectedEndDate is required")
    private LocalDate expectedEndDate;

    @NotBlank(message = "applicationReason is required")
    private String applicationReason;

    @NotBlank(message = "homeDescription is required")
    private String homeDescription;

    private Boolean hasExperience;

    private String experienceDetails;
}
