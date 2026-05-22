package com.petsave.petsave.Controller;

import com.petsave.petsave.Service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class EmailTestController {

    private final EmailService emailService;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${spring.mail.password}")
    private String mailPassword;

    public EmailTestController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/email-config")
    public ResponseEntity<Map<String, String>> checkEmailConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("mailUsername", mailUsername != null ? mailUsername : "NULL");
        config.put("mailPassword", mailPassword != null ? "SET" : "NULL");
        config.put("emailService", emailService != null ? "AVAILABLE" : "NULL");
        
        log.info("Email configuration check - Username: {}, Password: {}", 
                mailUsername != null ? "SET" : "NULL", 
                mailPassword != null ? "SET" : "NULL");
        
        return ResponseEntity.ok(config);
    }

    @PostMapping("/send-test-email")
    public ResponseEntity<Map<String, String>> sendTestEmail(@RequestParam(required = false) String toEmail) {
        Map<String, String> response = new HashMap<>();
        
        String testEmail = toEmail != null ? toEmail : "test@example.com";
        
        try {
            emailService.sendPaymentConfirmationEmail(
                testEmail,
                "Test User",
                50.00,
                "TEST-REF-123"
            );
            
            response.put("status", "SUCCESS");
            response.put("message", "Test email sent successfully to: " + testEmail);
            response.put("check", "Please check your inbox (and spam folder)");
            
            log.info("Test email sent successfully to: {}", testEmail);
            
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Failed to send test email: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            
            log.error("Failed to send test email to {}: {}", testEmail, e.getMessage(), e);
        }
        
        return ResponseEntity.ok(response);
    }
}
