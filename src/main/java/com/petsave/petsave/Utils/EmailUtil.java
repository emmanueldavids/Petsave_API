package com.petsave.petsave.Utils;

import com.petsave.petsave.Service.AsyncEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailUtil {

    private final AsyncEmailService asyncEmailService;

    public void sendVerificationEmail(String to, String code) {
        asyncEmailService.sendVerificationEmailAsync(to, code);
    }

    public void sendPasswordResetEmail(String to, String code) {
        asyncEmailService.sendPasswordResetEmailAsync(to, code);
    }
}
