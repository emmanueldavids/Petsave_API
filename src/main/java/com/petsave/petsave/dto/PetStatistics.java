package com.petsave.petsave.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PetStatistics {
    private Long totalPets;
    private Long availablePets;
    private Long rescuedDonationPets;
    private Long forAdoptionPets;
    private Long adoptedPets;
    private Long inTreatmentPets;
    private Long fosterCarePets;
    private Long dogs;
    private Long cats;
    private Long others;
}
