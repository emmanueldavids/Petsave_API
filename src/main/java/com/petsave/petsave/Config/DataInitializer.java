package com.petsave.petsave.Config;

import com.petsave.petsave.Entity.User;
import com.petsave.petsave.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "admin@petsave.com";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "Admin@123!";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeAdminUser();
    }

    private void initializeAdminUser() {
        try {
            User existingAdmin = userRepository.findByEmail(ADMIN_EMAIL)
                    .or(() -> userRepository.findByUsername(ADMIN_USERNAME))
                    .orElse(null);

            if (existingAdmin != null) {
                existingAdmin.setEmail(ADMIN_EMAIL);
                existingAdmin.setUsername(ADMIN_USERNAME);
                existingAdmin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
                existingAdmin.setRole("ADMIN");
                existingAdmin.setVerified(true);
                userRepository.save(existingAdmin);
                log.info("Admin user verified: {}", existingAdmin.getEmail());
                log.info("Admin credentials - Email: {}, Password: {}", ADMIN_EMAIL, ADMIN_PASSWORD);
                return;
            }

            User adminUser = User.builder()
                    .name("System Administrator")
                    .username(ADMIN_USERNAME)
                    .email(ADMIN_EMAIL)
                    .password(passwordEncoder.encode(ADMIN_PASSWORD))
                    .role("ADMIN")
                    .isVerified(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            User savedAdmin = userRepository.save(adminUser);
            log.info("Successfully created admin user: {}", savedAdmin.getEmail());
            log.info("Admin credentials - Email: {}, Password: {}", ADMIN_EMAIL, ADMIN_PASSWORD);

        } catch (Exception e) {
            log.error("Error initializing admin user: {}", e.getMessage(), e);
        }
    }
}
