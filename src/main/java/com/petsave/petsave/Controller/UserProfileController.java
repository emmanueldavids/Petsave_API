package com.petsave.petsave.Controller;

import com.petsave.petsave.Entity.User;
import com.petsave.petsave.Service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

    private final UserService userService;

    /**
     * Get current user profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        try {
            // Get authentication from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "User not authenticated"
                ));
            }

            String userEmail;
            if (authentication.getPrincipal() instanceof UserDetails) {
                userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
            } else {
                userEmail = authentication.getName();
            }

            if (userEmail == null || userEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid user",
                    "message", "User email not found in authentication"
                ));
            }

            User user = userService.findByEmail(userEmail);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not found",
                    "message", "Unable to find user profile for email: " + userEmail
                ));
            }

            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("name", user.getName());
            profile.put("username", user.getUsername());
            profile.put("email", user.getEmail());
            profile.put("role", user.getRole());
            profile.put("isVerified", user.isVerified());
            profile.put("createdAt", user.getCreatedAt());
            profile.put("updatedAt", user.getUpdatedAt());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "profile", profile
            ));

        } catch (Exception e) {
            log.error("Error getting user profile: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get profile",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Update current user profile
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(
            @RequestBody Map<String, String> updateData) {
        try {
            // Get authentication from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "User not authenticated"
                ));
            }

            String userEmail;
            if (authentication.getPrincipal() instanceof UserDetails) {
                userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
            } else {
                userEmail = authentication.getName();
            }

            User user = userService.findByEmail(userEmail);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not found",
                    "message", "Unable to find user profile"
                ));
            }

            // Update allowed fields
            if (updateData.containsKey("name") && updateData.get("name") != null) {
                user.setName(updateData.get("name"));
            }
            if (updateData.containsKey("username") && updateData.get("username") != null) {
                String newUsername = updateData.get("username");
                // Check if username is already taken by another user
                User existingUser = userService.findByUsername(newUsername);
                if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "error", "Username already taken",
                        "message", "This username is already in use by another user"
                    ));
                }
                user.setUsername(newUsername);
            }
            if (updateData.containsKey("email") && updateData.get("email") != null) {
                String newEmail = updateData.get("email");
                // Check if email is already taken by another user
                User existingUser = userService.findByEmail(newEmail);
                if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "error", "Email already taken",
                        "message", "This email is already in use by another user"
                    ));
                }
                user.setEmail(newEmail);
                // Email change requires re-verification
                user.setVerified(false);
            }

            user.setUpdatedAt(java.time.LocalDateTime.now());
            User updatedUser = userService.save(user);

            Map<String, Object> profile = new HashMap<>();
            profile.put("id", updatedUser.getId());
            profile.put("name", updatedUser.getName());
            profile.put("username", updatedUser.getUsername());
            profile.put("email", updatedUser.getEmail());
            profile.put("role", updatedUser.getRole());
            profile.put("isVerified", updatedUser.isVerified());
            profile.put("createdAt", updatedUser.getCreatedAt());
            profile.put("updatedAt", updatedUser.getUpdatedAt());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Profile updated successfully",
                "profile", profile
            ));

        } catch (Exception e) {
            log.error("Error updating user profile: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to update profile",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Delete current user account
     */
    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount() {
        try {
            // Get authentication from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "User not authenticated"
                ));
            }

            String userEmail;
            if (authentication.getPrincipal() instanceof UserDetails) {
                userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
            } else {
                userEmail = authentication.getName();
            }

            User user = userService.findByEmail(userEmail);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not found",
                    "message", "Unable to find user account"
                ));
            }

            // Delete user account (this should cascade delete related data)
            userService.deleteUser(user.getId());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Account deleted successfully"
            ));

        } catch (Exception e) {
            log.error("Error deleting user account: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to delete account",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Logout user (clear server-side session if needed)
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            // Get authentication from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated()) {
                String userEmail;
                if (authentication.getPrincipal() instanceof UserDetails) {
                    userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
                } else {
                    userEmail = authentication.getName();
                }
                log.info("User {} logged out", userEmail);
            }

            // In a JWT-based system, logout is primarily client-side (clear tokens)
            // But we can add server-side logging or token invalidation if needed
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logged out successfully"
            ));

        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Logout failed",
                "message", e.getMessage()
            ));
        }
    }
}
