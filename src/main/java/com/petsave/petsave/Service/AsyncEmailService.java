package com.petsave.petsave.Service;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AsyncEmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String fromEmail;

    public AsyncEmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

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

            helper.setFrom(fromEmail);
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

            helper.setFrom(fromEmail);
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

            helper.setFrom(fromEmail);
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

    @Async
    public void sendGenericEmailAsync(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            
            // Embed logo
            embedLogo(helper);
            
            helper.setText(content, true);
            
            mailSender.send(message);
        } catch (Exception e) {
            // Log error but don't throw to prevent blocking
            System.err.println("Failed to send generic email: " + e.getMessage());
        }
    }

    @Async
    public void sendDonationConfirmationAsync(String to, String name, Double amount, String transactionId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Donation Confirmation - Thank You for Your Support! - PetSave");
            
            // Embed logo
            embedLogo(helper);
            
            Context context = new Context();
            context.setVariable("donorName", name);
            context.setVariable("donorEmail", to);
            context.setVariable("amount", amount);
            context.setVariable("transactionId", transactionId);
            context.setVariable("donationDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a")));
            
            String htmlContent = templateEngine.process("donation-confirmation", context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (Exception e) {
            // Log error but don't throw to prevent blocking
            System.err.println("Failed to send donation confirmation email: " + e.getMessage());
        }
    }
}
