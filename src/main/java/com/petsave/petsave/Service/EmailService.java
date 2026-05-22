package com.petsave.petsave.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
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
public class EmailService {

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

    @Value("${app.admin.email:admin@petsave.com}")
    private String adminEmail;

    public EmailService(TemplateEngine templateEngine) {
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

    public void sendPaymentConfirmationEmail(String toEmail, String donorName,
                                             double amount, String reference) {
        Context context = new Context();
        context.setVariable("donorName", donorName);
        context.setVariable("amount", amount);
        context.setVariable("reference", reference);
        context.setVariable("donationDate", LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a")));
        context.setVariable("frontendUrl", frontendUrl);

        String html = templateEngine.process("donation-confirmation", context);
        send(toEmail, "Donation Confirmation - Thank You for Your Support! - PetSave", html);
    }

    public void sendDonationReceiptEmail(String toEmail, String donorName,
                                         double amount, String reference,
                                         String paymentDate) {
        Context context = new Context();
        context.setVariable("donorName", donorName);
        context.setVariable("amount", amount);
        context.setVariable("reference", reference);
        context.setVariable("paymentDate", paymentDate);
        context.setVariable("frontendUrl", frontendUrl);

        String html = templateEngine.process("donation-receipt", context);
        send(toEmail, "Donation Receipt - PetSave", html);
    }

    public void sendAdminDonationNotification(String donorName, String donorEmail,
                                              double amount, String reference) {
        Context context = new Context();
        context.setVariable("donorName", donorName);
        context.setVariable("donorEmail", donorEmail);
        context.setVariable("amount", amount);
        context.setVariable("reference", reference);
        context.setVariable("donationDate", LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a")));
        context.setVariable("frontendUrl", frontendUrl);

        String html = templateEngine.process("admin-donation-notification", context);
        send(adminEmail, "New Donation Received - " + amount + " from " + donorName, html);
    }
}
