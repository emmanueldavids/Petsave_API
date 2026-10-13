package com.petsave.petsave.Service;

import com.petsave.petsave.Entity.Notification;
import com.petsave.petsave.Repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for handling real-time notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Send notification to specific user
     */
    public void sendNotificationToUser(String userEmail, String message, String type) {
        try {
            Notification notification = Notification.builder()
                    .userEmail(userEmail)
                    .message(message)
                    .type(Notification.NotificationType.valueOf(type))
                    .isRead(false)
                    .createdAt(java.time.LocalDateTime.now())
                    .build();
            
            notificationRepository.save(notification);
            
            log.info("Notification saved for {}: {}", userEmail, message);
            
        } catch (Exception e) {
            log.error("Error sending notification to {}: {}", userEmail, e.getMessage(), e);
        }
    }

    /**
     * Broadcast notification to all users
     */
    public void broadcastNotification(String message, String type) {
        try {
            // Get all active users and send to each
            List<String> activeUsers = notificationRepository.findActiveUsers();
            
            for (String userEmail : activeUsers) {
                Notification notification = Notification.builder()
                        .userEmail(userEmail)
                        .message(message)
                        .type(Notification.NotificationType.valueOf(type))
                        .isRead(false)
                        .createdAt(java.time.LocalDateTime.now())
                        .build();
                
                notificationRepository.save(notification);
            }
            
            log.info("Broadcast notification saved for {} users", activeUsers.size());
            
        } catch (Exception e) {
            log.error("Error broadcasting notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Get unread notifications count for user
     */
    public int getUnreadCount(String userEmail) {
        try {
            return (int) notificationRepository.countByUserEmailAndIsRead(userEmail, false);
            
        } catch (Exception e) {
            log.error("Error getting unread count for {}: {}", userEmail, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Get user notifications with pagination
     */
    public List<Notification> getUserNotifications(String userEmail, int page, int size) {
        try {
            return notificationRepository.findByUserEmailOrderByCreatedAtDesc(userEmail, 
                    org.springframework.data.domain.PageRequest.of(page, size));
            
        } catch (Exception e) {
            log.error("Error getting notifications for {}: {}", userEmail, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Mark notifications as read
     */
    public void markAsRead(String userEmail, Map<String, Object> notificationIds) {
        try {
            List<Long> ids = notificationIds.values().stream()
                    .map(id -> Long.valueOf(id.toString()))
                    .toList();
            
            List<Notification> notifications = notificationRepository.findByUserEmailAndIdIn(userEmail, ids);
            
            for (Notification notification : notifications) {
                notification.setIsRead(true);
                notification.setReadAt(java.time.LocalDateTime.now());
                notificationRepository.save(notification);
            }
            
            log.info("Marked {} notifications as read for user: {}", ids.size(), userEmail);
            
        } catch (Exception e) {
            log.error("Error marking notifications as read for {}: {}", userEmail, e.getMessage(), e);
        }
    }

    /**
     * Delete notification
     */
    public void deleteNotification(Long id, String userEmail) {
        try {
            notificationRepository.deleteByIdAndUserEmail(id, userEmail);
            log.info("Deleted notification {} for user: {}", id, userEmail);
            
        } catch (Exception e) {
            log.error("Error deleting notification {} for user {}: {}", id, userEmail, e.getMessage(), e);
        }
    }

    /**
     * Process incoming WebSocket message
     */
    public void processNotification(String message, String userEmail) {
        try {
            log.info("Processing WebSocket message from {}: {}", userEmail, message);
            
            // Here you can implement custom logic based on message content
            // For example: trigger specific actions, update user status, etc.
            
        } catch (Exception e) {
            log.error("Error processing notification: {}", e.getMessage(), e);
        }
    }
}
