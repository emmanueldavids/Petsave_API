package com.petsave.petsave.dto;


import lombok.Data;

@Data
public class ResetConfirmRequest {
    private String email;
    private String code;
    private String newPassword;
}
