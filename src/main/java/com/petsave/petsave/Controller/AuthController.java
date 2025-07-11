package com.petsave.petsave.Controller;


import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.petsave.petsave.Service.AuthService;
import com.petsave.petsave.dto.*;
import com.petsave.petsave.Entity.User;
import com.petsave.petsave.Repository.UserRepository;

import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody @Valid RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/verify")
    public AuthResponse verify(@RequestBody VerifyRequest request) {
        return authService.verifyEmail(request);
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


    @GetMapping("/logout")
    public AuthResponse logout() {
        return new AuthResponse("Logout successful. Please delete the token on client.");
    }
    

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = authService.getAllUsers();
        if (users.isEmpty()) {
            return ResponseEntity.ok("No users found.");
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> user = authService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
