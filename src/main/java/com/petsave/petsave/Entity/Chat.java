package com.petsave.petsave.Entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    @JsonIgnoreProperties({"posts", "comments", "chats"})
    private User user1;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    @JsonIgnoreProperties({"posts", "comments", "chats"})
    private User user2;
    
    @Column(name = "last_message")
    private String lastMessage;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;
    
    @Column(name = "user1_unread_count")
    private Integer user1UnreadCount = 0;
    
    @Column(name = "user2_unread_count")
    private Integer user2UnreadCount = 0;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // Helper methods
    public void updateLastMessage(String message) {
        this.lastMessage = message;
        this.lastMessageAt = LocalDateTime.now();
    }
    
    public void incrementUnreadCount(User user) {
        if (user.equals(user1)) {
            user2UnreadCount++;
        } else {
            user1UnreadCount++;
        }
    }
    
    public void markAsRead(User user) {
        if (user.equals(user1)) {
            user1UnreadCount = 0;
        } else {
            user2UnreadCount = 0;
        }
    }
    
    public Integer getUnreadCount(User user) {
        if (user.equals(user1)) {
            return user1UnreadCount;
        } else {
            return user2UnreadCount;
        }
    }
    
    public User getOtherUser(User currentUser) {
        if (currentUser.equals(user1)) {
            return user2;
        } else {
            return user1;
        }
    }
    
    // Get time ago string for last message
    public String getLastMessageTimeAgo() {
        if (lastMessageAt == null) return "No messages";
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(lastMessageAt, now).toMinutes();
        
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " min ago";
        if (minutes < 1440) return (minutes / 60) + " hours ago";
        if (minutes < 10080) return (minutes / 1440) + " days ago";
        return (minutes / 10080) + " weeks ago";
    }
}
