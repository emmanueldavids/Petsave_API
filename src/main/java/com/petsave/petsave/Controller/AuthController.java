package com.petsave.petsave.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.petsave.petsave.Service.AuthService;
import com.petsave.petsave.dto.*;
import com.petsave.petsave.Entity.User;
import com.petsave.petsave.Repository.UserRepository;
import com.petsave.petsave.Utils.JwtUtil;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${spring.mail.password}")
    private String mailPassword;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/verify-otp")
    public AuthResponse verifyOtp(@RequestBody @Valid VerifyOtpDTO request) {
        return authService.verifyOtp(request);
    }

    @PostMapping("/resend")
    public AuthResponse resend(@RequestParam String email) {
        return authService.resendCode(email);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody TokenRefreshRequest request) {
        return authService.refreshToken(request);
    }

    // Temporary debug endpoint for testing
    @PostMapping("/debug-login")
    public LoginResponse debugLogin(@RequestBody LoginRequest request) {
        // For testing purposes, bypass verification
        User user = userRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found. Please check your username or email."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // Generate tokens without checking verification
        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return new LoginResponse(
                accessToken,
                refreshToken,
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }

    // Create test user for development
    @PostMapping("/create-test-user")
    public AuthResponse createTestUser() {
        String testEmail = "testuser" + System.currentTimeMillis() + "@example.com";
        String testUsername = "testuser" + System.currentTimeMillis();
        
        if (userRepository.findByEmail(testEmail).isPresent()) {
            return new AuthResponse("Test user already exists");
        }

        User testUser = User.builder()
                .name("Test User")
                .username(testUsername)
                .email(testEmail)
                .password(passwordEncoder.encode("password123"))
                .role("USER")
                .isVerified(true)
                .build();

        userRepository.save(testUser);
        return new AuthResponse("Test user created successfully. Use " + testEmail + " / password123 to login.");
    }

    @PostMapping("/reset-password/request")
    public AuthResponse requestReset(@RequestBody ResetPasswordRequest request) {
        return authService.requestPasswordReset(request);
    }

    @PostMapping("/reset-password/confirm")
    public AuthResponse confirmReset(@RequestBody ResetConfirmRequest request) {
        return authService.confirmResetPassword(request);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return authService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/count")
    public ResponseEntity<Long> getUserCount() {
        Long count = authService.getUserCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/debug/email")
    public ResponseEntity<Map<String, String>> debugEmailConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("mailUsername", mailUsername != null ? mailUsername : "NULL");
        config.put("mailPassword", mailPassword != null ? "SET" : "NULL");
        return ResponseEntity.ok(config);
    }
}
