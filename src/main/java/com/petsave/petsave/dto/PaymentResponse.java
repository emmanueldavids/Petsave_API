package com.petsave.petsave.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private boolean success;
    private String message;
    private String authorizationUrl;
    private String reference;
    private String accessCode;
    
    public static PaymentResponse success(String authorizationUrl, String reference, String accessCode) {
        return new PaymentResponse(true, "Payment initialized successfully", authorizationUrl, reference, accessCode);
    }
    
    public static PaymentResponse error(String message) {
        return new PaymentResponse(false, message, null, null, null);
    }
}
