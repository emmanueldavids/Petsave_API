package com.petsave.petsave.Entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED;
    
    @JsonValue
    public String toJson() {
        return this.name();
    }
}
