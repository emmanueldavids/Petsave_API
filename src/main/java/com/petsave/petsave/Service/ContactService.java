package com.petsave.petsave.Service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.petsave.petsave.dto.ContactRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final JavaMailSender mailSender;

    public void sendContactEmail(ContactRequest contact) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("khoswift@gmail.com"); // Where the email is sent
        message.setSubject("New Contact Message from: " + contact.getName());
        message.setText("From: " + contact.getEmail() + "\n\nTitle: " +contact.getTitle() +"\nMessage:" + contact.getMessage());

        mailSender.send(message);
    }
}
