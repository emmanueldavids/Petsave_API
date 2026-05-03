package com.petsave.petsave.Controller;

import com.petsave.petsave.Entity.User;
import com.petsave.petsave.Service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    /**
     * Get total users count (public endpoint)
     */
    @GetMapping("/count")
    public ResponseEntity<?> getUserCount() {
        try {
            long totalUsers = userService.getTotalUsersCount();
            long verifiedUsers = userService.getVerifiedUsersCount();
            long adminUsers = userService.getUsersByRoleCount("ADMIN");
            long regularUsers = userService.getUsersByRoleCount("USER");

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("verifiedUsers", verifiedUsers);
            stats.put("adminUsers", adminUsers);
            stats.put("regularUsers", regularUsers);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", totalUsers,
                "stats", stats
            ));

        } catch (Exception e) {
            log.error("Error getting user count: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "An unexpected error occurred. Please try again."
            ));
        }
    }

    /**
     * Get total users count (admin only - more detailed)
     */
    @GetMapping("/admin/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserCountAdmin() {
        try {
            long totalUsers = userService.getTotalUsersCount();
            long verifiedUsers = userService.getVerifiedUsersCount();
            long adminUsers = userService.getUsersByRoleCount("ADMIN");
            long regularUsers = userService.getUsersByRoleCount("USER");

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("verifiedUsers", verifiedUsers);
            stats.put("adminUsers", adminUsers);
            stats.put("regularUsers", regularUsers);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", totalUsers,
                "stats", stats
            ));

        } catch (Exception e) {
            log.error("Error getting user count: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "An unexpected error occurred. Please try again."
            ));
        }
    }

    /**
     * Get all users (public endpoint for testing)
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsersPublic() {
        try {
            List<User> allUsers = userService.getAllUsers();
            
            // Convert users to safe format (excluding sensitive data)
            List<Map<String, Object>> userSummaries = allUsers.stream()
                .map(user -> {
                    Map<String, Object> userSummary = new HashMap<>();
                    userSummary.put("id", user.getId());
                    userSummary.put("name", user.getName());
                    userSummary.put("username", user.getUsername());
                    userSummary.put("email", user.getEmail());
                    userSummary.put("role", user.getRole());
                    userSummary.put("isVerified", user.isVerified());
                    userSummary.put("createdAt", user.getCreatedAt());
                    return userSummary;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "users", userSummaries
            ));

        } catch (Exception e) {
            log.error("Error getting all users: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "An unexpected error occurred. Please try again."
            ));
        }
    }

    /**
     * Get all users with pagination (admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // Get all users (for now, return all users without pagination)
            // In a real implementation, you would use pagination with userRepository.findAll(Pageable)
            List<com.petsave.petsave.Entity.User> allUsers = userService.getAllUsers();
            
            // Convert users to safe format (excluding sensitive data)
            List<Map<String, Object>> userSummaries = allUsers.stream()
                .map(user -> {
                    Map<String, Object> userSummary = new HashMap<>();
                    userSummary.put("id", user.getId());
                    userSummary.put("name", user.getName());
                    userSummary.put("username", user.getUsername());
                    userSummary.put("email", user.getEmail());
                    userSummary.put("role", user.getRole());
                    userSummary.put("isVerified", user.isVerified());
                    userSummary.put("createdAt", user.getCreatedAt());
                    return userSummary;
                })
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("users", userSummaries);
            response.put("totalUsers", allUsers.size());
            response.put("page", page);
            response.put("size", size);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", response
            ));

        } catch (Exception e) {
            log.error("Error getting all users: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "An unexpected error occurred. Please try again."
            ));
        }
    }

    /**
     * Get user by ID (admin only)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            return userService.findById(id)
                .map(user -> {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("id", user.getId());
                    userData.put("name", user.getName());
                    userData.put("username", user.getUsername());
                    userData.put("email", user.getEmail());
                    userData.put("role", user.getRole());
                    userData.put("isVerified", user.isVerified());
                    userData.put("createdAt", user.getCreatedAt());
                    userData.put("updatedAt", user.getUpdatedAt());

                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "user", userData
                    ));
                })
                .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error getting user by ID: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "An unexpected error occurred. Please try again."
            ));
        }
    }

    /**
     * Update user (admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> updateData) {
        try {
            return userService.findById(id)
                .map(user -> {
                    // Update allowed fields
                    if (updateData.containsKey("name")) {
                        user.setName((String) updateData.get("name"));
                    }
                    if (updateData.containsKey("username")) {
                        user.setUsername((String) updateData.get("username"));
                    }
                    if (updateData.containsKey("email")) {
                        user.setEmail((String) updateData.get("email"));
                    }
                    if (updateData.containsKey("role")) {
                        user.setRole((String) updateData.get("role"));
                    }
                    if (updateData.containsKey("isVerified")) {
                        user.setVerified((Boolean) updateData.get("isVerified"));
                    }

                    com.petsave.petsave.Entity.User updatedUser = userService.save(user);

                    Map<String, Object> userData = new HashMap<>();
                    userData.put("id", updatedUser.getId());
                    userData.put("name", updatedUser.getName());
                    userData.put("username", updatedUser.getUsername());
                    userData.put("email", updatedUser.getEmail());
                    userData.put("role", updatedUser.getRole());
                    userData.put("isVerified", updatedUser.isVerified());
                    userData.put("createdAt", updatedUser.getCreatedAt());
                    userData.put("updatedAt", updatedUser.getUpdatedAt());

                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "User updated successfully",
                        "user", userData
                    ));
                })
                .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error updating user: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "An unexpected error occurred. Please try again."
            ));
        }
    }

    /**
     * Delete user (admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            if (!userService.findById(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }

            userService.deleteUser(id);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User deleted successfully"
            ));

        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "An unexpected error occurred. Please try again."
            ));
        }
    }
}
