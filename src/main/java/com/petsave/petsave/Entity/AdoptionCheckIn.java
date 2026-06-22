package com.petsave.petsave.Entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "adoption_check_ins", indexes = {
    @Index(name = "idx_adoption_id", columnList = "adoption_id"),
    @Index(name = "idx_check_in_status", columnList = "status"),
    @Index(name = "idx_milestone", columnList = "milestone"),
    @Index(name = "idx_due_date", columnList = "dueDate"),
    @Index(name = "idx_submitted_at", columnList = "submittedAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdoptionCheckIn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adoption_id", nullable = false)
    private Adoption adoption;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdoptionCheckInMilestone milestone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AdoptionCheckInStatus status = AdoptionCheckInStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AdoptionCheckInHealthStatus healthStatus = AdoptionCheckInHealthStatus.NOT_SUBMITTED;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    private LocalDateTime submittedAt;

    @Column(columnDefinition = "TEXT")
    private String adopterNotes;

    private String photoUrl;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
