package com.petsave.petsave.Service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class AsyncEmailService {

    private final TemplateEngine templateEngine;

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    @Value("${app.frontend.url:https://petsave-frontend.vercel.app}")
    private String frontendUrl;

    public AsyncEmailService(TemplateEngine templateEngine) {
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
