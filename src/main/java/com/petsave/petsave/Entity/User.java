package com.petsave.petsave.Entity;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true),
    @Index(name = "idx_user_username", columnList = "username", unique = true),
    @Index(name = "idx_user_verified", columnList = "isVerified")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;

    private boolean isVerified;

    @Enumerated(EnumType.STRING)
    private OtpType otpType;

    private String verificationCode;
    private LocalDateTime verificationCodeExpiresAt;

    private String refreshToken;
    private LocalDateTime refreshTokenExpiry;

    private String resetCode;
    private LocalDateTime resetCodeExpiry;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Community relationships
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Post> posts = new HashSet<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "user1", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Chat> chatsAsUser1 = new HashSet<>();

    @OneToMany(mappedBy = "user2", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Chat> chatsAsUser2 = new HashSet<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Message> sentMessages = new HashSet<>();

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Message> receivedMessages = new HashSet<>();

    @Column(name = "bio")
    private String bio;

    @Column(name = "location")
    private String location;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "posts_count")
    private Integer postsCount = 0;

    @Column(name = "pets_adopted")
    private Integer petsAdopted = 0;

    @Column(name = "is_online")
    private Boolean isOnline = false;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    // Helper methods
    public Set<Chat> getAllChats() {
        Set<Chat> allChats = new HashSet<>();
        allChats.addAll(chatsAsUser1);
        allChats.addAll(chatsAsUser2);
        return allChats;
    }

    public void incrementPostsCount() {
        this.postsCount = (this.postsCount == null ? 0 : this.postsCount) + 1;
    }

    public void decrementPostsCount() {
        this.postsCount = Math.max(0, (this.postsCount == null ? 0 : this.postsCount) - 1);
    }

    public void markAsOnline() {
        this.isOnline = true;
        this.lastSeen = LocalDateTime.now();
    }

    public void markAsOffline() {
        this.isOnline = false;
        this.lastSeen = LocalDateTime.now();
    }

    public String getLastSeenTimeAgo() {
        if (lastSeen == null) return "Never";
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(lastSeen, now).toMinutes();
        
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " min ago";
        if (minutes < 1440) return (minutes / 60) + " hours ago";
        if (minutes < 10080) return (minutes / 1440) + " days ago";
        return (minutes / 10080) + " weeks ago";
    }

    // ✅ Required by UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // or use roles if you have them
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isVerified; // or true
    }

    @Override
    public String getUsername() {
        return email; // make sure JWT uses email as subject
    }
}
