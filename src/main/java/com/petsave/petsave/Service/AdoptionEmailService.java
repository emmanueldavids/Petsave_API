package com.petsave.petsave.Service;

import com.petsave.petsave.Entity.Adoption;
import com.petsave.petsave.Entity.Pet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import java.util.List;

/**
 * Service for sending adoption-related email notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdoptionEmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${app.frontend.url:http://localhost:3001}")
    private String frontendUrl;
    
    @Value("${app.admin.email:admin@petsave.com}")
    private String adminEmail;

    /**
     * Send email to adopter when application is received
     */
    public void sendApplicationReceivedEmail(Adoption adoption, Pet pet) {
        try {
            Context context = new Context();
            context.setVariable("adopterName", adoption.getAdopterName());
            context.setVariable("petName", pet.getName());
            context.setVariable("petType", pet.getType());
            context.setVariable("petBreed", pet.getBreed());
            context.setVariable("applicationDate", adoption.getApplicationDate());
            context.setVariable("applicationId", adoption.getId());
            context.setVariable("frontendUrl", frontendUrl);

            String subject = "Adoption Application Received - " + pet.getName();
            String template = "adoption-application-received";
            
            sendHtmlEmail(adoption.getAdopterEmail(), subject, template, context);
            
            log.info("Application received email sent to: {}", adoption.getAdopterEmail());
            
        } catch (Exception e) {
            log.error("Error sending application received email: {}", e.getMessage(), e);
        }
    }

    /**
     * Send email to admin when new application is submitted
     */
    public void sendNewApplicationAdminEmail(Adoption adoption, Pet pet) {
        try {
            Context context = new Context();
            context.setVariable("adopterName", adoption.getAdopterName());
            context.setVariable("adopterEmail", adoption.getAdopterEmail());
            context.setVariable("adopterPhone", adoption.getAdopterPhone());
            context.setVariable("petName", pet.getName());
            context.setVariable("petType", pet.getType());
            context.setVariable("petBreed", pet.getBreed());
            context.setVariable("petAge", pet.getAge());
            context.setVariable("applicationDate", adoption.getApplicationDate());
            context.setVariable("applicationId", adoption.getId());
            context.setVariable("adminUrl", frontendUrl + "/admin/applications");

            String subject = "New Adoption Application - " + pet.getName();
            String template = "admin-new-application";
            
            sendHtmlEmail(adminEmail, subject, template, context);
            
            log.info("New application admin email sent to: {}", adminEmail);
            
        } catch (Exception e) {
            log.error("Error sending new application admin email: {}", e.getMessage(), e);
        }
    }

    /**
     * Send email to adopter when application is approved
     */
    public void sendApplicationApprovedEmail(Adoption adoption, Pet pet) {
        try {
            Context context = new Context();
            context.setVariable("adopterName", adoption.getAdopterName());
            context.setVariable("petName", pet.getName());
            context.setVariable("petType", pet.getType());
            context.setVariable("petBreed", pet.getBreed());
            context.setVariable("petAge", pet.getAge());
            context.setVariable("approvalDate", java.time.LocalDateTime.now());
            context.setVariable("applicationId", adoption.getId());
            context.setVariable("frontendUrl", frontendUrl);

            String subject = "Adoption Application Approved! - " + pet.getName();
            String template = "adoption-application-approved";
            
            sendHtmlEmail(adoption.getAdopterEmail(), subject, template, context);
            
            log.info("Application approved email sent to: {}", adoption.getAdopterEmail());
            
        } catch (Exception e) {
            log.error("Error sending application approved email: {}", e.getMessage(), e);
        }
    }

    /**
     * Send email to adopter when application is rejected
     */
    public void sendApplicationRejectedEmail(Adoption adoption, Pet pet, String rejectionReason) {
        try {
            Context context = new Context();
            context.setVariable("adopterName", adoption.getAdopterName());
            context.setVariable("petName", pet.getName());
            context.setVariable("petType", pet.getType());
            context.setVariable("petBreed", pet.getBreed());
            context.setVariable("rejectionReason", rejectionReason);
            context.setVariable("rejectionDate", java.time.LocalDateTime.now());
            context.setVariable("applicationId", adoption.getId());
            context.setVariable("frontendUrl", frontendUrl + "/pets");

            String subject = "Adoption Application Update - " + pet.getName();
            String template = "adoption-application-rejected";
            
            sendHtmlEmail(adoption.getAdopterEmail(), subject, template, context);
            
            log.info("Application rejected email sent to: {}", adoption.getAdopterEmail());
            
        } catch (Exception e) {
            log.error("Error sending application rejected email: {}", e.getMessage(), e);
        }
    }

    /**
     * Send email to admin when application is approved
     */
    public void sendApplicationApprovedAdminEmail(Adoption adoption, Pet pet) {
        try {
            Context context = new Context();
            context.setVariable("adopterName", adoption.getAdopterName());
            context.setVariable("adopterEmail", adoption.getAdopterEmail());
            context.setVariable("petName", pet.getName());
            context.setVariable("petType", pet.getType());
            context.setVariable("approvalDate", java.time.LocalDateTime.now());
            context.setVariable("applicationId", adoption.getId());
            context.setVariable("adminUrl", frontendUrl + "/admin/applications");

            String subject = "Application Approved - " + pet.getName();
            String template = "admin-application-approved";
            
            sendHtmlEmail(adminEmail, subject, template, context);
            
            log.info("Application approved admin email sent to: {}", adminEmail);
            
        } catch (Exception e) {
            log.error("Error sending application approved admin email: {}", e.getMessage(), e);
        }
    }

    /**
     * Send email to admin when application is rejected
     */
    public void sendApplicationRejectedAdminEmail(Adoption adoption, Pet pet, String rejectionReason) {
        try {
            Context context = new Context();
            context.setVariable("adopterName", adoption.getAdopterName());
            context.setVariable("adopterEmail", adoption.getAdopterEmail());
            context.setVariable("petName", pet.getName());
            context.setVariable("rejectionReason", rejectionReason);
            context.setVariable("rejectionDate", java.time.LocalDateTime.now());
            context.setVariable("applicationId", adoption.getId());
            context.setVariable("adminUrl", frontendUrl + "/admin/applications");

            String subject = "Application Rejected - " + pet.getName();
            String template = "admin-application-rejected";
            
            sendHtmlEmail(adminEmail, subject, template, context);
            
            log.info("Application rejected admin email sent to: {}", adminEmail);
            
        } catch (Exception e) {
            log.error("Error sending application rejected admin email: {}", e.getMessage(), e);
        }
    }

    /**
     * Send email to adopter when adoption is completed
     */
    public void sendAdoptionCompletedEmail(Adoption adoption, Pet pet) {
        try {
            Context context = new Context();
            context.setVariable("adopterName", adoption.getAdopterName());
            context.setVariable("petName", pet.getName());
            context.setVariable("petType", pet.getType());
            context.setVariable("petBreed", pet.getBreed());
            context.setVariable("completionDate", java.time.LocalDateTime.now());
            context.setVariable("applicationId", adoption.getId());
            context.setVariable("frontendUrl", frontendUrl);

            String subject = "Congratulations! Adoption Completed - " + pet.getName();
            String template = "adoption-completed";
            
            sendHtmlEmail(adoption.getAdopterEmail(), subject, template, context);
            
            log.info("Adoption completed email sent to: {}", adoption.getAdopterEmail());
            
        } catch (Exception e) {
            log.error("Error sending adoption completed email: {}", e.getMessage(), e);
        }
    }

    /**
     * Send email to admin when adoption is completed
     */
    public void sendAdoptionCompletedAdminEmail(Adoption adoption, Pet pet) {
        try {
            Context context = new Context();
            context.setVariable("adopterName", adoption.getAdopterName());
            context.setVariable("adopterEmail", adoption.getAdopterEmail());
            context.setVariable("petName", pet.getName());
            context.setVariable("completionDate", java.time.LocalDateTime.now());
            context.setVariable("applicationId", adoption.getId());
            context.setVariable("adminUrl", frontendUrl + "/admin/applications");

            String subject = "Adoption Completed - " + pet.getName();
            String template = "admin-adoption-completed";
            
            sendHtmlEmail(adminEmail, subject, template, context);
            
            log.info("Adoption completed admin email sent to: {}", adminEmail);
            
        } catch (Exception e) {
            log.error("Error sending adoption completed admin email: {}", e.getMessage(), e);
        }
    }

    /**
     * Send HTML email using Thymeleaf template
     */
    private void sendHtmlEmail(String to, String subject, String template, Context context) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("noreply@petsave.com");

            String htmlContent = templateEngine.process(template, context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            
        } catch (Exception e) {
            log.error("Error sending HTML email to {}: {}", to, e.getMessage(), e);
            // Fallback to simple email
            sendSimpleEmail(to, subject, generateSimpleEmailContent(subject, context));
        }
    }

    /**
     * Send simple text email as fallback
     */
    private void sendSimpleEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            message.setFrom("noreply@petsave.com");

            mailSender.send(message);
            
        } catch (Exception e) {
            log.error("Error sending simple email to {}: {}", to, e.getMessage(), e);
        }
    }

    /**
     * Generate simple email content as fallback
     */
    private String generateSimpleEmailContent(String subject, Context context) {
        StringBuilder content = new StringBuilder();
        content.append("Dear ").append(context.getVariable("adopterName")).append(",\n\n");
        
        if (subject.contains("Application Received")) {
            content.append("Thank you for your adoption application for ")
                   .append(context.getVariable("petName"))
                   .append(". We have received your application and will review it shortly.\n\n");
            content.append("Application ID: ").append(context.getVariable("applicationId")).append("\n");
            content.append("Application Date: ").append(context.getVariable("applicationDate")).append("\n\n");
        } else if (subject.contains("Approved")) {
            content.append("Congratulations! Your adoption application for ")
                   .append(context.getVariable("petName"))
                   .append(" has been approved.\n\n");
            content.append("Please contact us to arrange for pickup.\n\n");
        } else if (subject.contains("Rejected")) {
            content.append("We regret to inform you that your adoption application for ")
                   .append(context.getVariable("petName"))
                   .append(" could not be approved at this time.\n\n");
            if (context.getVariable("rejectionReason") != null) {
                content.append("Reason: ").append(context.getVariable("rejectionReason")).append("\n\n");
            }
        }
        
        content.append("Thank you for your interest in pet adoption.\n");
        content.append("PetSave Team\n");
        content.append(frontendUrl);
        
        return content.toString();
    }

    /**
     * Send bulk email to multiple admins
     */
    public void sendBulkAdminEmail(List<String> adminEmails, String subject, String template, Context context) {
        for (String email : adminEmails) {
            sendHtmlEmail(email, subject, template, context);
        }
    }
}
