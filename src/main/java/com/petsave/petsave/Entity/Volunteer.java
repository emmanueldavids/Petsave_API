package com.petsave.petsave.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "volunteers")
@Data
@NoArgsConstructor
public class Volunteer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ElementCollection
    @CollectionTable(name = "volunteer_skills", joinColumns = @JoinColumn(name = "volunteer_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "skill")
    private Set<VolunteerSkill> skills = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VolunteerAvailability availability;

    private String location;

    @Column(length = 2000)
    private String bio;

    private String emergencyContact;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VolunteerStatus status = VolunteerStatus.PENDING;

    @Column(nullable = false)
    private Integer hoursLogged = 0;

    @Column(nullable = false)
    private Integer totalTasksCompleted = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BadgeLevel badgeLevel = BadgeLevel.NONE;

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
