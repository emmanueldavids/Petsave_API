package com.petsave.petsave.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for real-time notifications
 */
@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userEmail;
    
    @Column(nullable = false)
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    @Column(nullable = false)
    private Boolean isRead = false;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime readAt;
    
    // For admin notifications
    @Column(nullable = true)
    private String senderEmail;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private NotificationPriority priority;
    
    public enum NotificationType {
        DONATION_SUCCESS,
        DONATION_FAILED,
        ADOPTION_SUBMITTED,
        ADOPTION_APPROVED,
        ADOPTION_REJECTED,
        ADOPTION_COMPLETED,
        APPLICATION_RECEIVED,
        APPLICATION_UPDATE,
        SYSTEM_ANNOUNCEMENT,
        CHAT_MESSAGE,
        PAYMENT_CONFIRMATION,
        POST_LIKED,
        POST_COMMENTED,
        COMMENT_REPLIED,
        BOOKING_REQUESTED,
        BOOKING_ACCEPTED,
        BOOKING_REJECTED,
        BOOKING_CONFIRMED,
        BOOKING_PAYMENT_FAILED,
        BOOKING_ONGOING,
        BOOKING_CANCELLED,
        BOOKING_DISPUTED,
        DISPUTE_RESOLVED,
        BOOKING_COMPLETED,
        PAYOUT_RELEASED,
        SITTER_STATUS_UPDATE,
        REHOMING_APPLICATION_RECEIVED,
        REHOMING_APPLICATION_APPROVED,
        REHOMING_APPLICATION_REJECTED,
        FOSTER_APPLICATION_RECEIVED,
        FOSTER_STATUS_UPDATE,
        FOSTER_COMPLETED,
        VOLUNTEER_STATUS_UPDATE,
        VOLUNTEER_TASK_ASSIGNED,
        CHECKIN_REMINDER,
        CHECKIN_HEALTH_CONCERN,
        CHECKIN_OVERDUE,
        LOST_FOUND_EXPIRED
    }
    
    public enum NotificationPriority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }
}
