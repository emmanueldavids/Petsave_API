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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeAdminUser();
    }

    private void initializeAdminUser() {
        try {
            // Check if admin user already exists
            User existingAdmin = userRepository.findByUsername("admin").orElse(null);
            
            if (existingAdmin != null) {
                log.info("Admin user already exists: {}", existingAdmin.getUsername());
                return;
            }

            // Create admin user
            User adminUser = User.builder()
                    .name("System Administrator")
                    .username("admin")
                    .email("admin@petsave.com")
                    .password(passwordEncoder.encode("adminuser"))
                    .role("ADMIN")
                    .isVerified(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            User savedAdmin = userRepository.save(adminUser);
            log.info("Successfully created admin user: {}", savedAdmin.getUsername());
            log.info("Admin credentials - Username: {}, Password: {}", "admin", "adminuser");
            
        } catch (Exception e) {
            log.error("Error initializing admin user: {}", e.getMessage(), e);
        }
    }
}
