package com.petsave.petsave.Service;

import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AsyncEmailServiceSmtpSelectorTest {

    @Test
    void shouldUseSmtpWhenRunningInLocalDevelopmentProfile() {
        AsyncEmailService service = new AsyncEmailService(mock(TemplateEngine.class), mock(JavaMailSender.class));

        ReflectionTestUtils.setField(service, "activeProfiles", "local");
        ReflectionTestUtils.setField(service, "brevoApiKey", "");

        assertTrue(service.shouldUseSmtp());
    }
}
