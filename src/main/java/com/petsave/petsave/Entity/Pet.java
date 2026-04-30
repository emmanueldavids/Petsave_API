package com.petsave.petsave.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@Entity
@Table(name = "pets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String breed;
    
    @Column(nullable = false)
    private Integer age;
    
    @Column(nullable = false)
    private String description;
    
    @Column(nullable = false)
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetStatus status;
    
    @Column(nullable = false)
    private String location;
    
    private String medicalHistory;
    
    private String specialNeeds;
    
    @Column(nullable = false)
    private Boolean available;
    
    @Column(nullable = false)
    private String uploadedBy;
    
    @Column(nullable = false)
    private String rescueDate;
    
    private String adoptionDate;
    
    @Column(nullable = false)
    private Double adoptionFee;
    
    private String story;
    
    @Column(nullable = false)
    private Boolean vaccinated;
    
    @Column(nullable = false)
    private Boolean neutered;
    
    @Column(nullable = false)
    private Boolean houseTrained;
    
    private String temperament;
    
    private String goodWith;
    
    private Integer weight;
    
    private String color;
    
    private String microchipId;
    
    @Column(nullable = false)
    private Boolean requiresExperienced;
    
    private String adoptionRequirements;
    
    @Column(nullable = false)
    private Long viewCount;
    
    @Column(nullable = false)
    private Long favoriteCount;

    // Explicit getters and setters to fix Lombok compilation issues
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
}
