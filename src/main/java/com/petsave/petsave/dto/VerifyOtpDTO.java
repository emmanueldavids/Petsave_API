package com.petsave.petsave.dto;


import com.petsave.petsave.Entity.OtpType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerifyOtpDTO {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String code;

    @NotNull
    private OtpType otpType;
}
