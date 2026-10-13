package com.petsave.petsave.Service;

import com.petsave.petsave.Entity.User;
import com.petsave.petsave.Repository.UserRepository;
import com.petsave.petsave.Utils.EmailUtil;
import com.petsave.petsave.Utils.JwtUtil;
import com.petsave.petsave.dto.LoginRequest;
import com.petsave.petsave.dto.LoginResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceEmailLoginTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailUtil emailUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginShouldAcceptEmailCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@petsave.com");
        request.setPassword("StrongPass!123");

        User user = User.builder()
                .name("System Administrator")
                .username("admin")
                .email("admin@petsave.com")
                .password("encodedPassword")
                .role("ADMIN")
                .isVerified(true)
                .build();

        when(userRepository.findByEmail("admin@petsave.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("StrongPass!123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateAccessToken("admin@petsave.com")).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken("admin@petsave.com")).thenReturn("refresh-token");

        LoginResponse response = authService.login(request);

        assertEquals("admin@petsave.com", response.getEmail());
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
    }
}
