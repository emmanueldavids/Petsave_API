package com.petsave.petsave.Entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PetStatus {
    RESCUED_DONATION("Rescued through Donation"),
    FOR_ADOPTION("Available for Adoption"),
    PENDING_ADOPTION("Adoption Pending"),
    ADOPTED("Adopted"),
    IN_TREATMENT("Under Medical Treatment"),
    FOSTER_CARE("In Foster Care"),
    TEMPORARY_CARE("Temporary Care"),
    SPECIAL_NEEDS("Special Needs Care"),
    SENIOR_PET("Senior Pet"),
    EMERGENCY_RESCUE("Emergency Rescue");

    private final String displayName;

    PetStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    @JsonValue
    public String toJson() {
        return this.name();
    }
}
