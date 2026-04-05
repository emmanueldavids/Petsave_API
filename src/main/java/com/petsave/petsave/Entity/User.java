package com.petsave.petsave.Entity;

import java.time.LocalDateTime;
import java.util.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    @Column(name = "role")
    private String role = "USER";

    private String verificationCode;
    private LocalDateTime verificationCodeExpiresAt;

    private String refreshToken;
    private LocalDateTime refreshTokenExpiry;

    private String resetCode;
    private LocalDateTime resetCodeExpiry;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    

    // ✅ Required by UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)); // Use the role field
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
