package com.petsave.petsave.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AsyncEmailService {

    private final TemplateEngine templateEngine;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.from.email}")
    private String fromEmail;

    @Value("${brevo.from.name:PetSave}")
    private String fromName;

    @Value("${app.frontend.url:https://petsave-frontend.vercel.app}")
    private String frontendUrl;

    public AsyncEmailService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    private void send(String to, String subject, String html) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", brevoApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                "sender", Map.of("name", fromName, "email", fromEmail),
                "to", List.of(Map.of("email", to)),
                "subject", subject,
                "htmlContent", html
            );

            restTemplate.postForEntity(
                "https://api.brevo.com/v3/smtp/email",
                new HttpEntity<>(body, headers),
                String.class
            );
            log.info("Email sent to {} via Brevo: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
        }
    }

    @Async
    public void sendVerificationEmailAsync(String to, String code, String name) {
        Context context = new Context();
        context.setVariable("name", name != null ? name : "User");
        context.setVariable("verificationCode", code);
        context.setVariable("subject", "Email Verification");
        context.setVariable("loginUrl", frontendUrl + "/login");

        String html = templateEngine.process("email/EmailTemplate", context);
        send(to, "PetSave - Email Verification", html);
    }

    @Async
    public void sendPasswordResetEmailAsync(String to, String code, String name) {
        Context context = new Context();
        context.setVariable("name", name != null ? name : "User");
        context.setVariable("verificationCode", code);
        context.setVariable("subject", "Password Reset");
        context.setVariable("loginUrl", frontendUrl + "/reset-password");

        String html = templateEngine.process("email/EmailTemplate", context);
        send(to, "PetSave - Password Reset", html);
    }

    @Async
    public void sendWelcomeEmailAsync(String to, String name) {
        Context context = new Context();
        context.setVariable("name", name != null ? name : "User");
        context.setVariable("subject", "Welcome");
        context.setVariable("loginUrl", frontendUrl + "/login");

        String html = templateEngine.process("email/EmailTemplate", context);
        send(to, "Welcome to PetSave!", html);
    }

    @Async
    public void sendGenericEmailAsync(String to, String subject, String content) {
        send(to, subject, content);
    }

    @Async
    public void sendDonationConfirmationAsync(String to, String name, Double amount, String transactionId) {
        Context context = new Context();
        context.setVariable("donorName", name);
        context.setVariable("donorEmail", to);
        context.setVariable("amount", amount);
        context.setVariable("transactionId", transactionId);
        context.setVariable("donationDate", LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a")));

        String html = templateEngine.process("donation-confirmation", context);
        send(to, "Donation Confirmation - Thank You for Your Support! - PetSave", html);
    }
}
