package com.petsave.petsave.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "rehoming_applications")
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RehomingApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rehoming_id", nullable = false)
    @JsonIgnoreProperties({"owner", "approvedBy", "adoptedBy"})
    private PetRehoming rehoming;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    @JsonIgnoreProperties({"password", "verificationCode", "verificationCodeExpiresAt", "refreshToken", "refreshTokenExpiry", "resetCode", "resetCodeExpiry", "authorities"})
    private User applicant;

    @Column(length = 4000)
    private String applicationReason;

    @Column(length = 4000)
    private String homeDescription;

    private Boolean hasOtherPets;
    private Boolean hasChildren;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RehomingApplicationStatus status = RehomingApplicationStatus.PENDING;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
