package com.petsave.petsave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnedPetRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "petType is required")
    private String petType;

    private String breed;

    private String notes;
}
