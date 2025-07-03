package com.petsave.petsave.dto;


import lombok.Data;

@Data
public class TokenRefreshRequest {
    private String email;
    private String refreshToken;
}
