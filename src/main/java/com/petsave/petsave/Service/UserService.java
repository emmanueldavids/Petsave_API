package com.petsave.petsave.Service;

import com.petsave.petsave.Entity.User;
import com.petsave.petsave.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;

    /**
     * Find user by email
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * Find user by username
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * Find user by ID
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Save user
     */
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Delete user by ID
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        
        try {
            userRepository.deleteById(id);
            log.info("User with id {} deleted successfully", id);
        } catch (Exception e) {
            log.error("Error deleting user with id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete user: " + e.getMessage());
        }
    }

    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Check if username exists
     */
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Update user last login time
     */
    public void updateLastLogin(String email) {
        User user = findByEmail(email);
        if (user != null) {
            // Note: User entity doesn't have lastLogin field, but we can add it if needed
            // For now, we'll just update the updatedAt field
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    /**
     * Verify user email
     */
    public void verifyUserEmail(String email) {
        User user = findByEmail(email);
        if (user != null) {
            user.setVerified(true);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("User email verified: {}", email);
        }
    }

    /**
     * Update user password
     */
    public void updatePassword(String email, String encodedPassword) {
        User user = findByEmail(email);
        if (user != null) {
            user.setPassword(encodedPassword);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("Password updated for user: {}", email);
        }
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get user statistics
     */
    public long getTotalUsersCount() {
        return userRepository.count();
    }

    /**
     * Get verified users count
     */
    public long getVerifiedUsersCount() {
        return userRepository.countByIsVerified(true);
    }

    /**
     * Get users by role
     */
    public long getUsersByRoleCount(String role) {
        return userRepository.countByRole(role);
    }
}
