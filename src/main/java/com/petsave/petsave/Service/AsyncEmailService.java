package com.petsave.petsave.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import jakarta.activation.DataSource;
import jakarta.mail.util.ByteArrayDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class AsyncEmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    private void embedLogo(MimeMessageHelper helper) throws Exception {
        // Try to load logo from different possible locations
        String[] logoPaths = {
            "static/images/logo.png",
            "images/logo.png", 
            "logo.png",
            "static/images/petsave-logo.png",
            "images/petsave-logo.png",
            "petsave-logo.png"
        };

        boolean logoEmbedded = false;
        for (String logoPath : logoPaths) {
            try {
                ClassPathResource resource = new ClassPathResource(logoPath);
                if (resource.exists()) {
                    helper.addInline("logo", resource);
                    logoEmbedded = true;
                    System.out.println("✅ Logo embedded from: " + logoPath);
                    break;
                }
            } catch (Exception e) {
                // Continue to next path
            }
        }

        if (!logoEmbedded) {
            // If no logo found, create a simple text logo or use placeholder
            System.out.println("⚠️ No logo found in resources. Using text logo.");
            // You could add a text-based logo here or skip the logo entirely
        }
    }

    @Async
    public void sendVerificationEmailAsync(String to, String code, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject("PetSave - Email Verification");
            
            // Embed logo
            embedLogo(helper);
            
            Context context = new Context();
            context.setVariable("name", name != null ? name : "User");
            context.setVariable("verificationCode", code);
            context.setVariable("subject", "Email Verification");
            context.setVariable("loginUrl", "http://localhost:3001/login");
            
            String htmlContent = templateEngine.process("email/EmailTemplate", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (Exception e) {
            // Log error but don't throw to prevent blocking
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmailAsync(String to, String code, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject("PetSave - Password Reset");
            
            // Embed logo
            embedLogo(helper);
            
            Context context = new Context();
            context.setVariable("name", name != null ? name : "User");
            context.setVariable("verificationCode", code);
            context.setVariable("subject", "Password Reset");
            context.setVariable("loginUrl", "http://localhost:3001/reset-password");
            
            String htmlContent = templateEngine.process("email/EmailTemplate", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (Exception e) {
            // Log error but don't throw to prevent blocking
            System.err.println("Failed to send password reset email: " + e.getMessage());
        }
    }

    @Async
    public void sendWelcomeEmailAsync(String to, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject("Welcome to PetSave!");
            
            // Embed logo
            embedLogo(helper);
            
            Context context = new Context();
            context.setVariable("name", name != null ? name : "User");
            context.setVariable("subject", "Welcome");
            context.setVariable("loginUrl", "http://localhost:3001/login");
            
            String htmlContent = templateEngine.process("email/EmailTemplate", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (Exception e) {
            // Log error but don't throw to prevent blocking
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }
}
