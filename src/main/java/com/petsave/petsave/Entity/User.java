package com.petsave.petsave.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is mandatory")
    private String email;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Username is mandatory")
    private String username;

    private String password;

    private boolean isVerified;

    private String verificationCode;
    private LocalDateTime verificationCodeExpiresAt;

    private String refreshToken;
    private LocalDateTime refreshTokenExpiry;

    private String resetCode;
    private LocalDateTime resetCodeExpiry;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
