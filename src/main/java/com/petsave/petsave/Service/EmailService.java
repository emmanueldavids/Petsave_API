package com.petsave.petsave.Service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class EmailService {

    private final TemplateEngine templateEngine;

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    @Value("${app.frontend.url:https://petsave-frontend.vercel.app}")
    private String frontendUrl;

    @Value("${app.admin.email:admin@petsave.com}")
    private String adminEmail;

    public EmailService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    private void send(String to, String subject, String html) {
        try {
            Resend resend = new Resend(resendApiKey);
            CreateEmailOptions options = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject(subject)
                    .html(html)
                    .build();
            resend.emails().send(options);
            log.info("Email sent to {}: {}", to, subject);
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
