package com.petsave.petsave.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Table(name = "pets")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    
    @Column(nullable = false, length = 1000)
    private String description;
    
    private String imageUrl;
    
    // Temporarily removed LOB fields to fix serialization issue
    // @Lob
    // @Column(length = 1000000) // For large images
    // @JsonIgnore
    // private byte[] image;
    
    // @JsonIgnore
    // private String imageType;
    
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

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL)
    private List<Adoption> adoptions;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL)
    private List<Donation> donations;
}
