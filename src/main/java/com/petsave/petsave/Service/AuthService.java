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
        throw new RuntimeException("Email already exists");
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
    emailUtil.sendVerificationEmail(user.getEmail(), code);

    return new AuthResponse("Registration successful. Check email for verification code.");
}



    // ================= VERIFY OTP =================
    public AuthResponse verifyOtp(VerifyOtpDTO request) {

    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (!request.getCode().equals(user.getVerificationCode()))
        throw new RuntimeException("Invalid OTP");

    if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now()))
        throw new RuntimeException("OTP expired");

    if (user.getOtpType() != request.getOtpType())
        throw new RuntimeException("OTP type mismatch");

    if (request.getOtpType() == OtpType.EMAIL_VERIFICATION) {
        user.setVerified(true);
    }

    // clear OTP after use
    user.setVerificationCode(null);
    user.setOtpType(null);
    user.setVerificationCodeExpiresAt(null);

    userRepository.save(user);

    return new AuthResponse("OTP verified successfully.");
}


    // ================= RESEND OTP =================
    public AuthResponse resendCode(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isVerified()) {
            return new AuthResponse("User already verified.");
        }

        String newOtp = generateOtp();

        user.setVerificationCode(newOtp);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        emailUtil.sendVerificationEmail(user.getEmail(), newOtp);

        return new AuthResponse("New verification code sent.");
    }

    // ================= LOGIN =================
    public TokenResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isVerified()) {
            throw new RuntimeException("Email not verified");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusDays(7));
        userRepository.save(user);

        return new TokenResponse(accessToken, refreshToken);
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

        emailUtil.sendVerificationEmail(user.getEmail(), code);

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

    // ================= OTP HELPER =================
    private String generateOtp() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }
}
