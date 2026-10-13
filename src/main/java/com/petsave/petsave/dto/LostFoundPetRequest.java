package com.petsave.petsave.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LostFoundPetRequest {

    @NotBlank(message = "reportType is required (LOST or FOUND)")
    private String reportType;

    private String petName;

    @NotBlank(message = "petType is required")
    private String petType;

    private String petBreed;

    @NotBlank(message = "description is required")
    private String description;

    private String imageUrl;

    @NotBlank(message = "lastSeenLocation is required")
    private String lastSeenLocation;

    private String city;

    private Double latitude;
    private Double longitude;

    private String contactPhone;
    private String contactEmail;
}
