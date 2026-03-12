package com.petsave.petsave.Service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

import com.petsave.petsave.dto.ContactRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final JavaMailSender mailSender;

    public void sendContactEmail(ContactRequest contact) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo("khoswift@gmail.com");
            helper.setSubject("🐾 PetSave Contact: " + contact.getTitle());
            
            String htmlContent = String.format(
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>PetSave Contact</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "            background-color: #f5f5f5;\n" +
                "            margin: 0;\n" +
                "            padding: 20px;\n" +
                "        }\n" +
                "        .container {\n" +
                "            max-width: 600px;\n" +
                "            margin: 0 auto;\n" +
                "            background-color: white;\n" +
                "            padding: 30px;\n" +
                "            border-radius: 8px;\n" +
                "            box-shadow: 0 2px 4px rgba(0,0,0,0.1);\n" +
                "        }\n" +
                "        .header {\n" +
                "            text-align: center;\n" +
                "            margin-bottom: 30px;\n" +
                "        }\n" +
                "        .logo {\n" +
                "            width: 120px;\n" +
                "            height: auto;\n" +
                "        }\n" +
                "        .title {\n" +
                "            color: #2E86C1;\n" +
                "            font-size: 24px;\n" +
                "            margin-bottom: 20px;\n" +
                "        }\n" +
                "        .content {\n" +
                "            margin-bottom: 30px;\n" +
                "            line-height: 1.6;\n" +
                "        }\n" +
                "        .contact-info {\n" +
                "            background-color: #f8f9fa;\n" +
                "            border-left: 4px solid #2E86C1;\n" +
                "            padding: 15px;\n" +
                "            margin: 10px 0;\n" +
                "            border-radius: 4px;\n" +
                "        }\n" +
                "        .message-box {\n" +
                "            background-color: #e9ecef;\n" +
                "            padding: 20px;\n" +
                "            border-radius: 4px;\n" +
                "            margin: 15px 0;\n" +
                "        }\n" +
                "        .footer {\n" +
                "            font-size: 12px;\n" +
                "            color: gray;\n" +
                "            text-align: center;\n" +
                "            margin-top: 30px;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h1 style=\"color:#2E86C1; font-size:32px; margin:0; font-weight:bold;\">🐾 PetSave</h1>\n" +
                "            <h2 class=\"title\">New Contact Message</h2>\n" +
                "        </div>\n" +
                "        <div class=\"content\">\n" +
                "            <h3>📧 Contact Information</h3>\n" +
                "            <div class=\"contact-info\">\n" +
                "                <p><strong>👤 Name:</strong> %s</p>\n" +
                "                <p><strong>📧 Email:</strong> %s</p>\n" +
                "                <p><strong>📝 Subject:</strong> %s</p>\n" +
                "            </div>\n" +
                "            <h3>💬 Message</h3>\n" +
                "            <div class=\"message-box\">\n" +
                "                <p>%s</p>\n" +
                "            </div>\n" +
                "            <p style=\"margin-top: 20px; font-size: 14px; color: #666;\">\n" +
                "                <strong>🕐 Sent:</strong> %s\n" +
                "                <br><strong>🌐 From:</strong> PetSave Contact Form\n" +
                "            </p>\n" +
                "        </div>\n" +
                "        <div class=\"footer\">\n" +
                "            <p>&copy; 2024 PetSave. All rights reserved.</p>\n" +
                "            <p>This message was sent from the PetSave contact form.</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>",
                contact.getName(),
                contact.getEmail(),
                contact.getTitle(),
                contact.getMessage(),
                java.time.LocalDateTime.now().toString()
            );
            
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            // Fallback to simple email if HTML fails
            SimpleMailMessage simpleMessage = new SimpleMailMessage();
            simpleMessage.setTo("khoswift@gmail.com");
            simpleMessage.setSubject("🐾 PetSave Contact: " + contact.getTitle());
            simpleMessage.setText(String.format(
                "Name: %s\nEmail: %s\nSubject: %s\n\nMessage:\n%s",
                contact.getName(),
                contact.getEmail(),
                contact.getTitle(),
                contact.getMessage()
            ));
            mailSender.send(simpleMessage);
        }
    }
}
