package com.petsave.petsave.Entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PetType {
    DOG,
    CAT,
    BIRD,
    RABBIT,
    OTHER;
    
    @JsonValue
    public String toJson() {
        return this.name();
    }
}
