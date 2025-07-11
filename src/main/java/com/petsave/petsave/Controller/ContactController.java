package com.petsave.petsave.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.petsave.petsave.Service.ContactService;
import com.petsave.petsave.dto.ContactRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<?> sendContact(@RequestBody ContactRequest request) {
        contactService.sendContactEmail(request);
        return ResponseEntity.ok("Message sent successfully.");
    }
}
