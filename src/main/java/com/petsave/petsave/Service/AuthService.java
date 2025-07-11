package com.petsave.petsave.Service;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.petsave.petsave.Entity.User;
import com.petsave.petsave.Repository.UserRepository;
import com.petsave.petsave.Utils.EmailUtil;
import com.petsave.petsave.Utils.JwtUtil;
import com.petsave.petsave.dto.AuthResponse;
import com.petsave.petsave.dto.LoginRequest;
import com.petsave.petsave.dto.RegisterRequest;
import com.petsave.petsave.dto.ResetConfirmRequest;
import com.petsave.petsave.dto.ResetPasswordRequest;
import com.petsave.petsave.dto.TokenRefreshRequest;
import com.petsave.petsave.dto.TokenResponse;
import com.petsave.petsave.dto.VerifyRequest;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final EmailUtil emailUtil;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;


    public AuthResponse register(RegisterRequest request) {
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        String code = String.format("%06d", new Random().nextInt(999999));

        User user = User.builder()
                .name(request.getName())
                .username(request.getUsername())  // ✅ FIX: Set username here
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .isVerified(false)
                .verificationCode(code)
                .verificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        userRepo.save(user);
        emailUtil.sendVerificationEmail(user.getEmail(), code);

        return new AuthResponse("Registration successful. Check email for verification code.");
    }


    public AuthResponse verifyEmail(VerifyRequest request) {
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isVerified()) {
            return new AuthResponse("Email already verified.");
        }

        if (!user.getVerificationCode().equals(request.getCode())) {
            throw new RuntimeException("Invalid verification code.");
        }

        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification code expired.");
        }

        user.setVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepo.save(user);

        return new AuthResponse("Email verified successfully.");
    }

    public AuthResponse resendCode(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isVerified()) {
            return new AuthResponse("User already verified.");
        }

        String newCode = String.format("%06d", new Random().nextInt(999999));
        user.setVerificationCode(newCode);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        userRepo.save(user);

        emailUtil.sendVerificationEmail(user.getEmail(), newCode);
        return new AuthResponse("New verification code sent.");
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isVerified()) throw new RuntimeException("Email not verified");
        if (!encoder.matches(request.getPassword(), user.getPassword()))
            throw new RuntimeException("Invalid credentials");

        String accessToken = jwtUtil.generateRefreshToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusDays(7));
        userRepo.save(user);

        return new TokenResponse(accessToken, refreshToken);
    }

    public TokenResponse refreshToken(TokenRefreshRequest request) {
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!request.getRefreshToken().equals(user.getRefreshToken()))
            throw new RuntimeException("Invalid refresh token");

        if (user.getRefreshTokenExpiry().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Refresh token expired");

        String newAccessToken = jwtUtil.generateRefreshToken(user.getEmail());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        user.setRefreshToken(newRefreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusDays(7));
        userRepo.save(user);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    public AuthResponse requestPasswordReset(ResetPasswordRequest request) {
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String code = String.format("%06d", new Random().nextInt(999999));
        user.setResetCode(code);
        user.setResetCodeExpiry(LocalDateTime.now().plusMinutes(10));
        userRepo.save(user);

        emailUtil.sendVerificationEmail(user.getEmail(), code);
        return new AuthResponse("Reset code sent to email.");
    }

    public AuthResponse confirmResetPassword(ResetConfirmRequest request) {
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!request.getCode().equals(user.getResetCode()))
            throw new RuntimeException("Invalid reset code");

        if (user.getResetCodeExpiry().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Reset code expired");

        user.setPassword(encoder.encode(request.getNewPassword()));
        user.setResetCode(null);
        user.setResetCodeExpiry(null);
        userRepo.save(user);

        return new AuthResponse("Password reset successful.");
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }



    public TokenResponse logout() {
        // Invalidate the token on the client side
        return new TokenResponse("Logout successful. Please delete the token on client.", null);
    }


}
