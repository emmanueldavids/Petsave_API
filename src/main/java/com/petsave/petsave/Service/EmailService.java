package com.petsave.petsave.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3001}")
    private String frontendUrl;

    @Value("${app.admin.email:admin@petsave.com}")
    private String adminEmail;

    @Autowired
    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendPaymentConfirmationEmail(String toEmail, String donorName, 
                                           double amount, String reference) {
        try {
            Context context = new Context();
            context.setVariable("donorName", donorName);
            context.setVariable("amount", amount);
            context.setVariable("reference", reference);
            context.setVariable("donationDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a")));
            context.setVariable("frontendUrl", frontendUrl);

            String subject = "Donation Confirmation - Thank You for Your Support! - PetSave";
            String template = "donation-confirmation";
            
            String htmlContent = templateEngine.process(template, context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Donation confirmation email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send donation confirmation email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    public void sendDonationReceiptEmail(String toEmail, String donorName, 
                                       double amount, String reference, 
                                       String paymentDate) {
        try {
            Context context = new Context();
            context.setVariable("donorName", donorName);
            context.setVariable("amount", amount);
            context.setVariable("reference", reference);
            context.setVariable("paymentDate", paymentDate);
            context.setVariable("frontendUrl", frontendUrl);

            String subject = "Donation Receipt - PetSave";
            String template = "donation-receipt";
            
            String htmlContent = templateEngine.process(template, context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Donation receipt email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send donation receipt email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    public void sendAdminDonationNotification(String donorName, String donorEmail, 
                                            double amount, String reference) {
        try {
            Context context = new Context();
            context.setVariable("donorName", donorName);
            context.setVariable("donorEmail", donorEmail);
            context.setVariable("amount", amount);
            context.setVariable("reference", reference);
            context.setVariable("donationDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a")));
            context.setVariable("frontendUrl", frontendUrl);

            String subject = "New Donation Received - " + amount + " from " + donorName;
            String template = "admin-donation-notification";
            
            String htmlContent = templateEngine.process(template, context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(adminEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Admin donation notification sent successfully to: {}", adminEmail);
            
        } catch (Exception e) {
            log.error("Failed to send admin donation notification to {}: {}", adminEmail, e.getMessage(), e);
        }
    }
}
