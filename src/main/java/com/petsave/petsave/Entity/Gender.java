package com.petsave.petsave.Entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Gender {
    MALE,
    FEMALE,
    OTHER;
    
    @JsonValue
    public String toJson() {
        return this.name();
    }
}
