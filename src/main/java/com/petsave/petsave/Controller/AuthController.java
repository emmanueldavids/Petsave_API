package com.petsave.petsave.Controller;

import com.petsave.petsave.Service.AuthService;
import com.petsave.petsave.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/verify-otp")
    public AuthResponse verifyOtp(@Valid @RequestBody VerifyOtpDTO request) {
        return authService.verifyOtp(request);
    }

    @PostMapping({"/resend", "/resend-otp"})
    public AuthResponse resend(
            @RequestParam(required = false) String email,
            @RequestBody(required = false) java.util.Map<String, String> body) {
        String resolvedEmail = (email != null) ? email : (body != null ? body.get("email") : null);
        if (resolvedEmail == null || resolvedEmail.isBlank()) {
            throw new RuntimeException("Email is required.");
        }
        return authService.resendCode(resolvedEmail);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
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
}
