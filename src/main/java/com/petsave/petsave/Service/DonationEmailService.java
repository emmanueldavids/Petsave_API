package com.petsave.petsave.Service;

import com.petsave.petsave.Utils.EmailUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonationEmailService {

    private final EmailUtil emailUtil;
    private final TemplateEngine templateEngine;

    /**
     * Send donation confirmation email to donor
     */
    public void sendDonationConfirmation(String donorEmail, String donorName, 
                                       Double amount, String transactionId) {
        try {
            Context context = new Context();
            context.setVariable("donorName", donorName);
            context.setVariable("donorEmail", donorEmail);
            context.setVariable("amount", amount);
            context.setVariable("transactionId", transactionId);
            context.setVariable("donationDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a")));

            String emailContent = templateEngine.process("donation-confirmation", context);
            
            String subject = "Donation Confirmation - Thank You for Your Support! - PetSave";
            
            emailUtil.sendEmail(donorEmail, subject, emailContent);
            
            log.info("Donation confirmation email sent to: {} for amount: {}", donorEmail, amount);
            
        } catch (Exception e) {
            log.error("Error sending donation confirmation email to {}: {}", donorEmail, e.getMessage(), e);
        }
    }

    /**
     * Send donation confirmation email with custom date
     */
    public void sendDonationConfirmation(String donorEmail, String donorName, 
                                       Double amount, String transactionId, LocalDateTime donationDate) {
        try {
            Context context = new Context();
            context.setVariable("donorName", donorName);
            context.setVariable("donorEmail", donorEmail);
            context.setVariable("amount", amount);
            context.setVariable("transactionId", transactionId);
            context.setVariable("donationDate", donationDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a")));

            String emailContent = templateEngine.process("donation-confirmation", context);
            
            String subject = "Donation Confirmation - Thank You for Your Support! - PetSave";
            
            emailUtil.sendEmail(donorEmail, subject, emailContent);
            
            log.info("Donation confirmation email sent to: {} for amount: {} on {}", donorEmail, amount, donationDate);
            
        } catch (Exception e) {
            log.error("Error sending donation confirmation email to {}: {}", donorEmail, e.getMessage(), e);
        }
    }

    /**
     * Send donation receipt email (alternative template if needed)
     */
    public void sendDonationReceipt(String donorEmail, String donorName, 
                                  Double amount, String transactionId, Map<String, Object> additionalData) {
        try {
            Context context = new Context();
            context.setVariable("donorName", donorName);
            context.setVariable("donorEmail", donorEmail);
            context.setVariable("amount", amount);
            context.setVariable("transactionId", transactionId);
            context.setVariable("donationDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a")));
            
            // Add any additional data
            additionalData.forEach(context::setVariable);

            String emailContent = templateEngine.process("donation-confirmation", context);
            
            String subject = "Donation Receipt - PetSave";
            
            emailUtil.sendEmail(donorEmail, subject, emailContent);
            
            log.info("Donation receipt email sent to: {} for amount: {}", donorEmail, amount);
            
        } catch (Exception e) {
            log.error("Error sending donation receipt email to {}: {}", donorEmail, e.getMessage(), e);
        }
    }

    /**
     * Send donation notification to admin
     */
    public void notifyAdminAboutDonation(String donorEmail, String donorName, 
                                       Double amount, String transactionId) {
        try {
            Context context = new Context();
            context.setVariable("donorName", donorName);
            context.setVariable("donorEmail", donorEmail);
            context.setVariable("amount", amount);
            context.setVariable("transactionId", transactionId);
            context.setVariable("donationDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a")));

            String emailContent = templateEngine.process("admin-donation-notification", context);
            
            String subject = "New Donation Received - " + amount + " from " + donorName;
            
            emailUtil.sendEmail("admin@petsave.com", subject, emailContent);
            
            log.info("Admin notification sent for donation: {} from {}", amount, donorEmail);
            
        } catch (Exception e) {
            log.error("Error sending admin donation notification: {}", e.getMessage(), e);
        }
    }
}
