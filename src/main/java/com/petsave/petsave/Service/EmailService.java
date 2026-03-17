package com.petsave.petsave.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPaymentConfirmationEmail(String toEmail, String donorName, 
                                           double amount, String reference) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Payment Confirmation - PetSave Donation");
            
            String emailBody = buildPaymentConfirmationEmail(donorName, amount, reference);
            message.setText(emailBody);

            mailSender.send(message);
            log.info("Payment confirmation email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send payment confirmation email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    public void sendDonationReceiptEmail(String toEmail, String donorName, 
                                       double amount, String reference, 
                                       String paymentDate) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Donation Receipt - PetSave");
            
            String emailBody = buildDonationReceiptEmail(donorName, amount, reference, paymentDate);
            message.setText(emailBody);

            mailSender.send(message);
            log.info("Donation receipt email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send donation receipt email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    private String buildPaymentConfirmationEmail(String donorName, double amount, String reference) {
        return String.format("""
            Dear %s,
            
            Thank you for your generous donation to PetSave!
            
            Payment Details:
            - Amount: $%.2f
            - Reference: %s
            - Status: Successful
            
            Your contribution will help us rescue and care for more animals in need.
            We will send you a detailed receipt shortly.
            
            Thank you for supporting our mission!
            
            Best regards,
            The PetSave Team
            
            ---
            PetSave - Saving Animals, Changing Lives
            """, donorName, amount, reference);
    }

    private String buildDonationReceiptEmail(String donorName, double amount, 
                                           String reference, String paymentDate) {
        return String.format("""
            Dear %s,
            
            Your Donation Receipt from PetSave
            
            Receipt Details:
            - Donor Name: %s
            - Donation Amount: $%.2f
            - Transaction Reference: %s
            - Payment Date: %s
            - Status: Completed
            
            Your generous donation will directly support:
            - Animal rescue operations
            - Medical care for rescued animals
            - Food and shelter expenses
            - Adoption programs
            
            Thank you for being a hero to animals in need!
            
            For tax purposes, this email serves as your official donation receipt.
            
            With gratitude,
            The PetSave Team
            
            ---
            PetSave - Saving Animals, Changing Lives
            Website: www.petsave.com
            Email: info@petsave.com
            """, donorName, donorName, amount, reference, paymentDate);
    }
}
