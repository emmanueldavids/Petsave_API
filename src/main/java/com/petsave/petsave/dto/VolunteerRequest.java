package com.petsave.petsave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolunteerRequest {

    @NotEmpty(message = "At least one skill is required")
    private List<String> skills;

    @NotNull(message = "Availability is required")
    private String availability;

    @NotBlank(message = "Location is required")
    private String location;

    private String bio;

    private String emergencyContact;
}
