package com.petsave.petsave.Controller;

import com.petsave.petsave.Config.NotificationWebSocketHandler;
import com.petsave.petsave.Service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

/**
 * REST Controller for real-time notifications
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationWebSocketHandler webSocketHandler;

    /**
     * Send real-time notification to specific user
     */
    @PostMapping("/api/notifications/send")
    public ResponseEntity<?> sendNotification(
            @RequestBody Map<String, Object> notificationData,
            Principal principal) {
        
        try {
            String recipientEmail = (String) notificationData.get("recipientEmail");
            String message = (String) notificationData.get("message");
            String type = (String) notificationData.get("type");
            
            log.info("Sending notification from {} to {}: {}", principal.getName(), recipientEmail);
            
            // Persist the notification
            notificationService.sendNotificationToUser(recipientEmail, message, type);

            // Deliver in real-time to just the intended recipient (if they're online)
            webSocketHandler.sendToUser(recipientEmail, Map.of(
                "type", type,
                "message", message,
                "recipient", recipientEmail,
                "sender", principal.getName(),
                "timestamp", System.currentTimeMillis()
            ));
            
            return ResponseEntity.ok(Map.of(
                "message", "Notification sent successfully",
                "recipient", recipientEmail
            ));
            
        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to send notification",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Get unread notifications count for user
     */
    @GetMapping("/api/notifications/unread/count")
    public ResponseEntity<?> getUnreadCount(Principal principal) {
        try {
            String userEmail = principal.getName();
            int unreadCount = notificationService.getUnreadCount(userEmail);
            
            return ResponseEntity.ok(Map.of(
                "unreadCount", unreadCount,
                "user", userEmail
            ));
            
        } catch (Exception e) {
            log.error("Error getting unread count: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get unread count",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Mark notifications as read
     */
    @PostMapping("/api/notifications/mark-read")
    public ResponseEntity<?> markAsRead(
            @RequestBody Map<String, Object> notificationIds,
            Principal principal) {
        
        try {
            String userEmail = principal.getName();
            notificationService.markAsRead(userEmail, notificationIds);
            
            return ResponseEntity.ok(Map.of(
                "message", "Notifications marked as read",
                "count", notificationIds.size()
            ));
            
        } catch (Exception e) {
            log.error("Error marking notifications as read: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to mark notifications as read",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Get user notifications
     */
    @GetMapping("/api/notifications")
    public ResponseEntity<?> getUserNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {
        
        try {
            String userEmail = principal.getName();
            var notifications = notificationService.getUserNotifications(userEmail, page, size);
            
            return ResponseEntity.ok(Map.of(
                "notifications", notifications,
                "user", userEmail
            ));
            
        } catch (Exception e) {
            log.error("Error getting user notifications: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get notifications",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Create notification for multiple users (admin function)
     */
    @PostMapping("/api/notifications/broadcast")
    public ResponseEntity<?> broadcastNotification(
            @RequestBody Map<String, Object> notificationData,
            Principal principal) {
        
        try {
            String message = (String) notificationData.get("message");
            String type = (String) notificationData.get("type");
            
            log.info("Broadcasting notification from admin {}: {}", principal.getName());
            
            // Broadcast to all connected users
            notificationService.broadcastNotification(message, type);
            
            // Broadcast via WebSocket
            webSocketHandler.broadcastMessage(Map.of(
                "type", type,
                "message", message,
                "sender", principal.getName(),
                "timestamp", System.currentTimeMillis()
            ));
            
            return ResponseEntity.ok(Map.of(
                "message", "Notification broadcast successfully",
                "type", type,
                "activeConnections", webSocketHandler.getActiveConnectionCount()
            ));
            
        } catch (Exception e) {
            log.error("Error broadcasting notification: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to broadcast notification",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Delete notification
     */
    @DeleteMapping("/api/notifications/{id}")
    public ResponseEntity<?> deleteNotification(
            @PathVariable Long id,
            Principal principal) {
        
        try {
            String userEmail = principal.getName();
            notificationService.deleteNotification(id, userEmail);
            
            return ResponseEntity.ok(Map.of(
                "message", "Notification deleted successfully"
            ));
            
        } catch (Exception e) {
            log.error("Error deleting notification: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to delete notification",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Get WebSocket connection status
     */
    @GetMapping("/api/notifications/connections")
    public ResponseEntity<?> getConnectionStatus(Principal principal) {
        try {
            return ResponseEntity.ok(Map.of(
                "activeConnections", webSocketHandler.getActiveConnectionCount(),
                "user", principal.getName(),
                "timestamp", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            log.error("Error getting connection status: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get connection status",
                "message", e.getMessage()
            ));
        }
    }
}
