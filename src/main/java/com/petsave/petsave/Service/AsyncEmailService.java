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

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    @Value("${brevo.from.email:noreply@petsave.com}")
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

    // ================= ADOPTION CHECK-IN EMAILS =================
    @Async
    public void sendCheckInReminderEmailAsync(String to, String adopterName, String petName, String milestone, LocalDateTime dueDate) {
        Context context = new Context();
        context.setVariable("name", adopterName);
        context.setVariable("petName", petName);
        context.setVariable("milestone", milestone.replaceAll("_", " ").toLowerCase());
        context.setVariable("dueDate", dueDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        context.setVariable("subject", "Pet Health Check-in Reminder");
        context.setVariable("checkInUrl", frontendUrl + "/dashboard/adoptions");

        String html = templateEngine.process("email/check-in-reminder", context);
        send(to, "PetSave - " + petName + " Health Check-in Reminder", html);
    }

    @Async
    public void sendCheckInConfirmationEmailAsync(String to, String adopterName, String milestone) {
        Context context = new Context();
        context.setVariable("name", adopterName);
        context.setVariable("milestone", milestone.replaceAll("_", " ").toLowerCase());
        context.setVariable("subject", "Check-in Submitted");
        context.setVariable("dashboardUrl", frontendUrl + "/dashboard/adoptions");

        String html = templateEngine.process("email/check-in-confirmation", context);
        send(to, "PetSave - Health Check-in Received", html);
    }

    @Async
    public void sendAdminMissedCheckInAlertAsync(String adopterName, String adopterEmail, String petName, String milestone, LocalDateTime dueDate) {
        try {
            String adminEmail = System.getenv("ADMIN_EMAIL");
            if (adminEmail == null) {
                adminEmail = "admin@petsave.com";
            }

            Context context = new Context();
            context.setVariable("adopterName", adopterName);
            context.setVariable("adopterEmail", adopterEmail);
            context.setVariable("petName", petName);
            context.setVariable("milestone", milestone.replaceAll("_", " ").toLowerCase());
            context.setVariable("dueDate", dueDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
            context.setVariable("overdueSince", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
            context.setVariable("subject", "Missed Pet Health Check-in");
            context.setVariable("adminDashboardUrl", frontendUrl + "/admin/check-ins");

            String html = templateEngine.process("email/admin-missed-check-in", context);
            send(adminEmail, "⚠️ PetSave Admin Alert - Missed Check-in: " + petName, html);

            log.warn("Admin alert sent for missed check-in: {} ({})", petName, adopterEmail);
        } catch (Exception e) {
            log.error("Failed to send admin missed check-in alert: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendAdminHealthConcernAlertAsync(String petName, String adopterName, String healthStatus, String notes) {
        try {
            String adminEmail = System.getenv("ADMIN_EMAIL");
            if (adminEmail == null) {
                adminEmail = "admin@petsave.com";
            }

            Context context = new Context();
            context.setVariable("petName", petName);
            context.setVariable("adopterName", adopterName);
            context.setVariable("healthStatus", healthStatus.replaceAll("_", " ").toLowerCase());
            context.setVariable("notes", notes != null ? notes : "No additional notes provided");
            context.setVariable("subject", "Pet Health Concern Alert");
            context.setVariable("adminDashboardUrl", frontendUrl + "/admin/check-ins");

            String html = templateEngine.process("email/admin-health-concern", context);
            send(adminEmail, "⚠️ PetSave Admin Alert - Health Concern: " + petName, html);

            log.warn("Admin alert sent for health concern: {} ({})", petName, healthStatus);
        } catch (Exception e) {
            log.error("Failed to send admin health concern alert: {}", e.getMessage(), e);
        }
    }
}
