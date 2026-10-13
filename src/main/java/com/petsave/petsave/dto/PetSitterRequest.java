package com.petsave.petsave.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetSitterRequest {

    private String bio;

    private String profileImageUrl;

    private Integer serviceRadius;

    private String location;

    private String city;

    @NotEmpty(message = "At least one accepted pet type is required")
    private List<String> acceptedPetTypes;

    @NotNull(message = "ratePerDay is required")
    @Positive(message = "ratePerDay must be positive")
    private BigDecimal ratePerDay;

    private BigDecimal ratePerNight;

    private Integer maxPetsAtOnce;

    // Optional bank details for payout — required before any real Paystack transfer can succeed
    private String bankCode;

    private String accountNumber;

    private String accountName;
}
