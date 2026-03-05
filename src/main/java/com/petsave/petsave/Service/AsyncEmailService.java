package com.petsave.petsave.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncEmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendVerificationEmailAsync(String to, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("PetSave - Email Verification");
            message.setText("Your verification code is: " + code + "\n\nThis code will expire in 15 minutes.");
            
            mailSender.send(message);
        } catch (Exception e) {
            // Log error but don't throw to prevent blocking
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmailAsync(String to, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("PetSave - Password Reset");
            message.setText("Your password reset code is: " + code + "\n\nThis code will expire in 10 minutes.");
            
            mailSender.send(message);
        } catch (Exception e) {
            // Log error but don't throw to prevent blocking
            System.err.println("Failed to send password reset email: " + e.getMessage());
        }
    }
}
