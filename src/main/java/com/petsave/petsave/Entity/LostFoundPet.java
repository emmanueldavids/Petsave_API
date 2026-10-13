package com.petsave.petsave.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "lost_found_pets")
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LostFoundPet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    @JsonIgnoreProperties({"password", "verificationCode", "verificationCodeExpiresAt", "refreshToken", "refreshTokenExpiry", "resetCode", "resetCodeExpiry", "authorities"})
    private User reporter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LostFoundReportType reportType;

    // Nullable — a FOUND report often has no name for the pet yet
    private String petName;

    @Column(nullable = false)
    private String petType;

    private String petBreed;

    @Column(length = 2000)
    private String description;

    private String imageUrl;

    @Column(length = 2000)
    private String lastSeenLocation;

    private String city;

    private Double latitude;
    private Double longitude;

    private String contactPhone;
    private String contactEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LostFoundStatus status = LostFoundStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (expiresAt == null) {
            expiresAt = createdAt.plusDays(60);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
