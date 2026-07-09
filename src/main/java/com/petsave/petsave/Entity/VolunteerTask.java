package com.petsave.petsave.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "volunteer_tasks")
@Data
@NoArgsConstructor
public class VolunteerTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_id", nullable = false)
    private Volunteer volunteer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id")
    private Pet pet;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VolunteerTaskType taskType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VolunteerTaskStatus status = VolunteerTaskStatus.ASSIGNED;

    private LocalDateTime scheduledDate;
    private LocalDateTime completedDate;

    private Integer hoursSpent;

    @Column(length = 2000)
    private String adminNotes;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
