package com.petsave.petsave.Controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.petsave.petsave.Service.FusionAuthService;
import com.petsave.petsave.dto.LoginRequest;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private FusionAuthService fusionAuthService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam String email, @RequestParam String password) {
        boolean registered = fusionAuthService.registerUser(email, password);
        if (registered) {
            return ResponseEntity.ok(Map.of("message", "User registered successfully"));
        }
        return ResponseEntity.status(500).body(Map.of("error", "Registration failed"));
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Map<String, Object> response = fusionAuthService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
        
        if (response.containsKey("token")) {
            return ResponseEntity.ok(Map.of(
                "access_token", response.get("token"),
                "userId", ((Map) response.get("user")).get("id")
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam String userId) {
        boolean loggedOut = fusionAuthService.logoutUser(userId);
        if (loggedOut) {
            return ResponseEntity.ok(Map.of("message", "User logged out successfully"));
        }
        return ResponseEntity.status(500).body(Map.of("error", "Logout failed"));
    }
}
