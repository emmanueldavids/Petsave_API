package com.petsave.petsave.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "pet_rehomings")
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PetRehoming {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnoreProperties({"password", "verificationCode", "verificationCodeExpiresAt", "refreshToken", "refreshTokenExpiry", "resetCode", "resetCodeExpiry", "authorities"})
    private User owner;

    @Column(nullable = false)
    private String petName;

    @Column(nullable = false)
    private String petType;

    private String petBreed;
    private Integer petAge;

    @Column(length = 2000)
    private String description;

    private String imageUrl;

    @Column(length = 4000)
    private String reason;

    @Column(length = 4000)
    private String medicalHistory;

    private Boolean vaccinated;
    private Boolean neutered;

    private String temperament;
    private String goodWith;
    private String specialNeeds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RehomingStatus status = RehomingStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adopted_by_id")
    private User adoptedBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
