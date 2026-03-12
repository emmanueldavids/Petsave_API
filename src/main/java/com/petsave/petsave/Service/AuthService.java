package com.petsave.petsave.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.petsave.petsave.Entity.OtpType;
import com.petsave.petsave.Entity.User;
import com.petsave.petsave.Repository.UserRepository;
import com.petsave.petsave.Utils.EmailUtil;
import com.petsave.petsave.Utils.JwtUtil;
import com.petsave.petsave.dto.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final EmailUtil emailUtil;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // ================= REGISTER =================
    public AuthResponse register(RegisterRequest request) {

    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
        throw new RuntimeException("Email already exists. Please use a different email address.");
    }

    if (userRepository.findByUsername(request.getUsername()).isPresent()) {
        throw new RuntimeException("Username already exists. Please choose a different username.");
    }

    String code = String.format("%06d", new Random().nextInt(999999));

    User user = User.builder()
            .name(request.getName())
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .isVerified(false)
            .verificationCode(code)
            .otpType(OtpType.EMAIL_VERIFICATION)
            .verificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15))
            .build();

    userRepository.save(user);
    emailUtil.sendVerificationEmail(user.getEmail(), code, user.getName());

    return new AuthResponse("Registration successful. Check email for verification code.");
}



    // ================= VERIFY OTP =================
    public AuthResponse verifyOtp(VerifyOtpDTO request) {

    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found. Please check your email address."));

    if (!request.getCode().equals(user.getVerificationCode()))
        throw new RuntimeException("Invalid verification code. Please check your email and try again.");

    if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now()))
        throw new RuntimeException("Verification code has expired. Please request a new one.");

    if (user.getOtpType() != request.getOtpType())
        throw new RuntimeException("Invalid verification code type. Please request a new code.");

    if (request.getOtpType() == OtpType.EMAIL_VERIFICATION) {
        user.setVerified(true);
        // clear OTP after email verification
        user.setVerificationCode(null);
        user.setOtpType(null);
        user.setVerificationCodeExpiresAt(null);
        
        // Send welcome email after successful verification
        emailUtil.sendWelcomeEmail(user.getEmail(), user.getName());
    }
    
    // For PASSWORD_RESET, keep the OTP for the reset confirmation step
    // It will be cleared after password reset is confirmed

    userRepository.save(user);

    return new AuthResponse("OTP verified successfully.");
}


    // ================= RESEND OTP =================
    public AuthResponse resendCode(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found. Please check your email address."));

        if (user.isVerified()) {
            return new AuthResponse("Your email is already verified. You can proceed to login.");
        }

        String newOtp = generateOtp();

        user.setVerificationCode(newOtp);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        emailUtil.sendVerificationEmail(user.getEmail(), newOtp, user.getName());

        return new AuthResponse("New verification code sent.");
    }

    // ================= LOGIN =================
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found. Please check your username or email."));

        if (!user.isVerified()) {
            throw new RuntimeException("Please verify your email address before logging in. Check your inbox for the verification code.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password. Please try again.");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusDays(7));
        userRepository.save(user);

        return new LoginResponse(
            accessToken, 
            refreshToken, 
            user.getName(), 
            user.getUsername(), 
            user.getEmail(), 
            "USER" // Default role for now
        );
    }

    // ================= REFRESH TOKEN =================
    public TokenResponse refreshToken(TokenRefreshRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!request.getRefreshToken().equals(user.getRefreshToken())) {
            throw new RuntimeException("Invalid refresh token");
        }

        if (user.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        user.setRefreshToken(newRefreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusDays(7));
        userRepository.save(user);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    // ================= PASSWORD RESET =================
    public AuthResponse requestPasswordReset(ResetPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // String otp = generateOtp();
        String code = String.format("%06d", new Random().nextInt(999999));

        user.setVerificationCode(code);
        user.setOtpType(OtpType.PASSWORD_RESET);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        emailUtil.sendPasswordResetEmail(user.getEmail(), code, user.getName());

        return new AuthResponse("Reset OTP sent to email.");
    }

    //Confirm Password Reset
    public AuthResponse confirmResetPassword(ResetConfirmRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!request.getCode().equals(user.getVerificationCode())) {
            throw new RuntimeException("Invalid reset code");
        }

        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset code expired");
        }

        if (user.getOtpType() != OtpType.PASSWORD_RESET) {
            throw new RuntimeException("Invalid OTP type");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        user.setOtpType(null);
        userRepository.save(user);

        return new AuthResponse("Password reset successful.");
    }

    // ================= USERS =================
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Long getUserCount() {
        return userRepository.count();
    }

    // ================= OTP HELPER =================
    private String generateOtp() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }
}
