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
        ADOPTION_APPROVED,
        ADOPTION_REJECTED,
        APPLICATION_RECEIVED,
        APPLICATION_UPDATE,
        SYSTEM_ANNOUNCEMENT,
        CHAT_MESSAGE,
        PAYMENT_CONFIRMATION
    }
    
    public enum NotificationPriority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }
}
