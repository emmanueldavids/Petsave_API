package com.petsave.petsave.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.petsave.petsave.Service.AuthService;
import com.petsave.petsave.dto.*;
import com.petsave.petsave.Entity.User;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
    public TokenResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody TokenRefreshRequest request) {
        return authService.refreshToken(request);
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
}
