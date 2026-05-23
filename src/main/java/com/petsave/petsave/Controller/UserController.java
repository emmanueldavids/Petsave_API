package com.petsave.petsave.Controller;

import com.petsave.petsave.Entity.User;
import com.petsave.petsave.Service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    // ---- Current user profile ----

    @GetMapping("/me")
    public ResponseEntity<?> getProfile() {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        return ResponseEntity.ok(Map.of("success", true, "profile", toProfileMap(user)));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> updateData) {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        if (updateData.containsKey("name") && updateData.get("name") != null) {
            user.setName(updateData.get("name"));
        }
        if (updateData.containsKey("username") && updateData.get("username") != null) {
            String newUsername = updateData.get("username");
            User existing = userService.findByUsername(newUsername);
            if (existing != null && !existing.getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username already taken"));
            }
            user.setUsername(newUsername);
        }
        if (updateData.containsKey("email") && updateData.get("email") != null) {
            String newEmail = updateData.get("email");
            User existing = userService.findByEmail(newEmail);
            if (existing != null && !existing.getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already taken"));
            }
            user.setEmail(newEmail);
            user.setVerified(false);
        }

        user.setUpdatedAt(LocalDateTime.now());
        User updated = userService.save(user);
        return ResponseEntity.ok(Map.of("success", true, "message", "Profile updated", "profile", toProfileMap(updated)));
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteAccount() {
        User user = getCurrentUser();
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        userService.deleteUser(user.getId());
        return ResponseEntity.ok(Map.of("success", true, "message", "Account deleted successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getPrincipal() instanceof UserDetails u ? u.getUsername() : auth.getName();
            log.info("User {} logged out", email);
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "Logged out successfully"));
    }

    // ---- Public stats ----

    @GetMapping("/count")
    public ResponseEntity<?> getUserCount() {
        long total = userService.getTotalUsersCount();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", total);
        stats.put("verifiedUsers", userService.getVerifiedUsersCount());
        stats.put("adminUsers", userService.getUsersByRoleCount("ADMIN"));
        stats.put("regularUsers", userService.getUsersByRoleCount("USER"));
        return ResponseEntity.ok(Map.of("success", true, "count", total, "stats", stats));
    }

    // ---- Admin CRUD ----

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<User> users = userService.getAllUsers();
        List<Map<String, Object>> summaries = users.stream().map(this::toProfileMap).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("success", true, "data",
                Map.of("users", summaries, "totalUsers", users.size(), "page", page, "size", size)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(u -> ResponseEntity.ok(Map.of("success", true, "user", toProfileMap(u))))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        return userService.findById(id).map(user -> {
            if (data.containsKey("name")) user.setName((String) data.get("name"));
            if (data.containsKey("username")) user.setUsername((String) data.get("username"));
            if (data.containsKey("email")) user.setEmail((String) data.get("email"));
            if (data.containsKey("role")) user.setRole((String) data.get("role"));
            if (data.containsKey("isVerified")) user.setVerified((Boolean) data.get("isVerified"));
            User updated = userService.save(user);
            return ResponseEntity.ok(Map.of("success", true, "message", "User updated", "user", toProfileMap(updated)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (userService.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "User deleted successfully"));
    }

    // ---- Helpers ----

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        String email = auth.getPrincipal() instanceof UserDetails u ? u.getUsername() : auth.getName();
        return userService.findByEmail(email);
    }

    private Map<String, Object> toProfileMap(User user) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", user.getId());
        m.put("name", user.getName());
        m.put("username", user.getUsernameValue());
        m.put("email", user.getEmail());
        m.put("role", user.getRole());
        m.put("isVerified", user.isVerified());
        m.put("createdAt", user.getCreatedAt());
        m.put("updatedAt", user.getUpdatedAt());
        return m;
    }
}
