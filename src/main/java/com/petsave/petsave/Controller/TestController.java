package com.petsave.petsave.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${spring.mail.password}")
    private String mailPassword;

    @PostMapping("/email")
    public ResponseEntity<Map<String, String>> testEmail() {
        Map<String, String> result = new HashMap<>();
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(mailUsername); // Send to self for testing
            message.setSubject("Test Email");
            message.setText("This is a test email from PetSave API");
            
            mailSender.send(message);
            
            result.put("status", "success");
            result.put("message", "Email sent successfully");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
            result.put("username", mailUsername);
            result.put("passwordSet", mailPassword != null ? "yes" : "no");
            return ResponseEntity.status(500).body(result);
        }
    }
}
