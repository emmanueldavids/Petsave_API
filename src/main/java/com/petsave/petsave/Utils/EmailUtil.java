package com.petsave.petsave.Utils;

import com.petsave.petsave.Service.AsyncEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailUtil {

    private final AsyncEmailService asyncEmailService;

    public void sendVerificationEmail(String to, String code, String name) {
        asyncEmailService.sendVerificationEmailAsync(to, code, name);
    }

    public void sendPasswordResetEmail(String to, String code, String name) {
        asyncEmailService.sendPasswordResetEmailAsync(to, code, name);
    }

    public void sendWelcomeEmail(String to, String name) {
        asyncEmailService.sendWelcomeEmailAsync(to, name);
    }
}
