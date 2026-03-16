package com.petsave.petsave.dto;

import com.petsave.petsave.Entity.PetType;
import jakarta.validation.constraints.*;

public class AdoptionRequest {
    
    @NotNull(message = "Pet ID is required")
    private Long petId;
    
    @NotBlank(message = "Pet name is required")
    private String petName;
    
    @NotBlank(message = "Pet breed is required")
    private String petBreed;
    
    @NotNull(message = "Pet age is required")
    @Min(value = 0, message = "Pet age cannot be negative")
    @Max(value = 50, message = "Pet age seems unrealistic")
    private Integer petAge;
    
    @NotBlank(message = "Pet description is required")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String petDescription;
    
    private String petImageUrl;
    
    @NotNull(message = "Pet type is required")
    private PetType petType;
    
    @NotBlank(message = "Adopter name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String adopterName;
    
    @NotBlank(message = "Adopter email is required")
    @Email(message = "Invalid email format")
    private String adopterEmail;
    
    @NotBlank(message = "Adopter phone is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number format")
    private String adopterPhone;
    
    @NotBlank(message = "Adopter address is required")
    @Size(max = 200, message = "Address must not exceed 200 characters")
    private String adopterAddress;
    
    @NotBlank(message = "Application reason is required")
    @Size(max = 500, message = "Application reason must not exceed 500 characters")
    private String applicationReason;

    // Constructors
    public AdoptionRequest() {}

    public AdoptionRequest(
        String petName, String petBreed, Integer petAge, String petDescription,
        String petImageUrl, PetType petType, String adopterName, String adopterEmail,
        String adopterPhone, String adopterAddress, String applicationReason
    ) {
        this.petName = petName;
        this.petBreed = petBreed;
        this.petAge = petAge;
        this.petDescription = petDescription;
        this.petImageUrl = petImageUrl;
        this.petType = petType;
        this.adopterName = adopterName;
        this.adopterEmail = adopterEmail;
        this.adopterPhone = adopterPhone;
        this.adopterAddress = adopterAddress;
        this.applicationReason = applicationReason;
    }

    // Getters and Setters
    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getPetBreed() {
        return petBreed;
    }

    public void setPetBreed(String petBreed) {
        this.petBreed = petBreed;
    }

    public Integer getPetAge() {
        return petAge;
    }

    public void setPetAge(Integer petAge) {
        this.petAge = petAge;
    }

    public String getPetDescription() {
        return petDescription;
    }

    public void setPetDescription(String petDescription) {
        this.petDescription = petDescription;
    }

    public String getPetImageUrl() {
        return petImageUrl;
    }

    public void setPetImageUrl(String petImageUrl) {
        this.petImageUrl = petImageUrl;
    }

    public PetType getPetType() {
        return petType;
    }

    public void setPetType(PetType petType) {
        this.petType = petType;
    }

    public String getAdopterName() {
        return adopterName;
    }

    public void setAdopterName(String adopterName) {
        this.adopterName = adopterName;
    }

    public String getAdopterEmail() {
        return adopterEmail;
    }

    public void setAdopterEmail(String adopterEmail) {
        this.adopterEmail = adopterEmail;
    }

    public String getAdopterPhone() {
        return adopterPhone;
    }

    public void setAdopterPhone(String adopterPhone) {
        this.adopterPhone = adopterPhone;
    }

    public String getAdopterAddress() {
        return adopterAddress;
    }

    public void setAdopterAddress(String adopterAddress) {
        this.adopterAddress = adopterAddress;
    }

    public String getApplicationReason() {
        return applicationReason;
    }

    public void setApplicationReason(String applicationReason) {
        this.applicationReason = applicationReason;
    }
}
