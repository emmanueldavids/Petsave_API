package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Notification entity
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Find notifications by user email with pagination
     */
    List<Notification> findByUserEmailOrderByCreatedAtDesc(String userEmail, org.springframework.data.domain.Pageable pageable);
    
    /**
     * Count unread notifications for user
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userEmail = :userEmail AND n.isRead = :isRead")
    long countByUserEmailAndIsRead(@Param("userEmail") String userEmail, @Param("isRead") Boolean isRead);
    
    /**
     * Find notifications by user email and IDs
     */
    List<Notification> findByUserEmailAndIdIn(String userEmail, List<Long> ids);
    
    /**
     * Delete notification by ID and user email
     */
    void deleteByIdAndUserEmail(@Param("id") Long id, @Param("userEmail") String userEmail);
    
    /**
     * Find all active users for broadcasting
     */
    @Query("SELECT DISTINCT u.email FROM User u WHERE u.isVerified = true")
    List<String> findActiveUsers();
    
    /**
     * Find notifications by user email and read status with pagination
     */
    @Query("SELECT n FROM Notification n WHERE n.userEmail = :userEmail AND n.isRead = :isRead ORDER BY n.createdAt DESC")
    List<Notification> findByUserEmailAndIsReadOrderByCreatedAtDesc(@Param("userEmail") String userEmail, @Param("isRead") Boolean isRead, org.springframework.data.domain.Pageable pageable);
    
    /**
     * Find notification by ID and user email
     */
    Optional<Notification> findByIdAndUserEmail(Long id, String userEmail);
}
