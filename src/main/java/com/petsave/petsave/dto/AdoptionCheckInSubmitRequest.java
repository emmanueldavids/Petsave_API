package com.petsave.petsave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdoptionCheckInSubmitRequest {
    private String adopterNotes;
    private String healthStatus; // HEALTHY, NEEDS_VET, CONCERNS
}
