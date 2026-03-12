package com.petsave.petsave.dto;

import com.petsave.petsave.Entity.PetType;
import com.petsave.petsave.Entity.PetStatus;
import java.time.LocalDateTime;

public class PetResponse {
    private Long id;
    private String name;
    private String breed;
    private Integer age;
    private String description;
    private String imageUrl;
    private PetType type;
    private PetStatus status;
    private String location;
    private String medicalHistory;
    private String specialNeeds;
    private Boolean available;
    private String uploadedBy;
    private String rescueDate;
    private String adoptionDate;
    private Double adoptionFee;
    private String story;
    private Boolean vaccinated;
    private Boolean neutered;
    private Boolean houseTrained;
    private String temperament;
    private String goodWith;
    private Integer weight;
    private String color;
    private String microchipId;
    private Boolean requiresExperienced;
    private String adoptionRequirements;
    private Long viewCount;
    private Long favoriteCount;
    private LocalDateTime createdAt;

    // Constructors
    public PetResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }

    public Long getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Long favoriteCount) { this.favoriteCount = favoriteCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
