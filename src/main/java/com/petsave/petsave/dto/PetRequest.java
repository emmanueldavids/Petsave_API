package com.petsave.petsave.dto;

import com.petsave.petsave.Entity.PetType;
import com.petsave.petsave.Entity.PetStatus;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

public class PetRequest {
    
    @NotBlank(message = "Pet name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;
    
    @NotBlank(message = "Pet breed is required")
    @Size(max = 100, message = "Breed must not exceed 100 characters")
    private String breed;
    
    @NotNull(message = "Pet age is required")
    @Min(value = 0, message = "Pet age cannot be negative")
    @Max(value = 30, message = "Pet age seems unrealistic")
    private Integer age;
    
    @NotBlank(message = "Pet description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    private String imageUrl;
    
    private MultipartFile image; // For image upload
    
    @NotNull(message = "Pet type is required")
    private PetType type;
    
    @NotNull(message = "Pet status is required")
    private PetStatus status;
    
    @NotBlank(message = "Location is required")
    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;
    
    @Size(max = 1000, message = "Medical history must not exceed 1000 characters")
    private String medicalHistory;
    
    @Size(max = 500, message = "Special needs must not exceed 500 characters")
    private String specialNeeds;
    
    @NotNull(message = "Available status is required")
    private Boolean available;
    
    @NotBlank(message = "Uploaded by is required")
    private String uploadedBy;
    
    @NotBlank(message = "Rescue date is required")
    private String rescueDate;
    
    private String adoptionDate;
    
    @NotNull(message = "Adoption fee is required")
    @Min(value = 0, message = "Adoption fee cannot be negative")
    private Double adoptionFee;
    
    @Size(max = 2000, message = "Story must not exceed 2000 characters")
    private String story;
    
    @NotNull(message = "Vaccinated status is required")
    private Boolean vaccinated;
    
    @NotNull(message = "Neutered status is required")
    private Boolean neutered;
    
    @NotNull(message = "House trained status is required")
    private Boolean houseTrained;
    
    @Size(max = 200, message = "Temperament must not exceed 200 characters")
    private String temperament;
    
    @Size(max = 200, message = "Good with information must not exceed 200 characters")
    private String goodWith;
    
    @Min(value = 0, message = "Weight cannot be negative")
    private Integer weight;
    
    @Size(max = 50, message = "Color must not exceed 50 characters")
    private String color;
    
    @Size(max = 50, message = "Microchip ID must not exceed 50 characters")
    private String microchipId;
    
    @NotNull(message = "Requires experienced status is required")
    private Boolean requiresExperienced;
    
    @Size(max = 500, message = "Adoption requirements must not exceed 500 characters")
    private String adoptionRequirements;

    // Constructors
    public PetRequest() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public MultipartFile getImage() { return image; }
    public void setImage(MultipartFile image) { this.image = image; }

    public PetType getType() { return type; }
    public void setType(PetType type) { this.type = type; }

    public PetStatus getStatus() { return status; }
    public void setStatus(PetStatus status) { this.status = status; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }

    public String getSpecialNeeds() { return specialNeeds; }
    public void setSpecialNeeds(String specialNeeds) { this.specialNeeds = specialNeeds; }

    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public String getRescueDate() { return rescueDate; }
    public void setRescueDate(String rescueDate) { this.rescueDate = rescueDate; }

    public String getAdoptionDate() { return adoptionDate; }
    public void setAdoptionDate(String adoptionDate) { this.adoptionDate = adoptionDate; }

    public Double getAdoptionFee() { return adoptionFee; }
    public void setAdoptionFee(Double adoptionFee) { this.adoptionFee = adoptionFee; }

    public String getStory() { return story; }
    public void setStory(String story) { this.story = story; }

    public Boolean getVaccinated() { return vaccinated; }
    public void setVaccinated(Boolean vaccinated) { this.vaccinated = vaccinated; }

    public Boolean getNeutered() { return neutered; }
    public void setNeutered(Boolean neutered) { this.neutered = neutered; }

    public Boolean getHouseTrained() { return houseTrained; }
    public void setHouseTrained(Boolean houseTrained) { this.houseTrained = houseTrained; }

    public String getTemperament() { return temperament; }
    public void setTemperament(String temperament) { this.temperament = temperament; }

    public String getGoodWith() { return goodWith; }
    public void setGoodWith(String goodWith) { this.goodWith = goodWith; }

    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getMicrochipId() { return microchipId; }
    public void setMicrochipId(String microchipId) { this.microchipId = microchipId; }

    public Boolean getRequiresExperienced() { return requiresExperienced; }
    public void setRequiresExperienced(Boolean requiresExperienced) { this.requiresExperienced = requiresExperienced; }

    public String getAdoptionRequirements() { return adoptionRequirements; }
    public void setAdoptionRequirements(String adoptionRequirements) { this.adoptionRequirements = adoptionRequirements; }
}
