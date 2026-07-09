package com.petsave.petsave.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolunteerTaskCompleteRequest {

    @NotNull(message = "hoursSpent is required")
    @Min(value = 0, message = "hoursSpent cannot be negative")
    private Integer hoursSpent;
}
