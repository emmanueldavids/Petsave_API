package com.petsave.petsave.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "foster_applications")
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FosterApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    @JsonIgnoreProperties({"password", "verificationCode", "verificationCodeExpiresAt", "refreshToken", "refreshTokenExpiry", "resetCode", "resetCodeExpiry", "authorities"})
    private User applicant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    private LocalDate startDate;
    private LocalDate expectedEndDate;

    @Column(length = 2000)
    private String applicationReason;

    @Column(length = 2000)
    private String homeDescription;

    private Boolean hasExperience;

    @Column(length = 2000)
    private String experienceDetails;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FosterApplicationStatus status = FosterApplicationStatus.PENDING;

    @Column(length = 2000)
    private String adminNotes;

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
