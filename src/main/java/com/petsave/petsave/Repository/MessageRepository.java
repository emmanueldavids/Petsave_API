package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.Chat;
import com.petsave.petsave.Entity.Message;
import com.petsave.petsave.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    List<Message> findByChatOrderByCreatedAtAsc(Chat chat);
    
    Page<Message> findByChatOrderByCreatedAtAsc(Chat chat, Pageable pageable);
    
    List<Message> findBySenderOrderByCreatedAtDesc(User sender);
    
    List<Message> findByReceiverOrderByCreatedAtDesc(User receiver);
    
    Page<Message> findByChatAndIsDeletedFalseOrderByCreatedAtAsc(Chat chat, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.chat = :chat AND m.isDeleted = false ORDER BY m.createdAt DESC")
    List<Message> findLatestMessagesByChat(@Param("chat") Chat chat);
    
    @Query("SELECT m FROM Message m WHERE (m.sender = :user OR m.receiver = :user) AND m.isDeleted = false ORDER BY m.createdAt DESC")
    Page<Message> findMessagesByUser(@Param("user") User user, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.chat = :chat AND m.isDeleted = false AND m.createdAt >= :since ORDER BY m.createdAt ASC")
    List<Message> findMessagesSince(@Param("chat") Chat chat, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat = :chat AND m.isDeleted = false")
    Long countMessagesByChat(@Param("chat") Chat chat);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver = :user AND m.isRead = false AND m.isDeleted = false")
    Long countUnreadMessages(@Param("user") User user);
    
    @Query("SELECT m FROM Message m WHERE m.chat = :chat AND m.isDeleted = false AND " +
           "(LOWER(m.content) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Message> searchMessagesInChat(@Param("chat") Chat chat, @Param("search") String search, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.isDeleted = false AND " +
           "(LOWER(m.content) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.sender.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.receiver.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Message> searchAllMessages(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.chat = :chat AND m.isDeleted = false AND m.isRead = false AND m.receiver = :user")
    List<Message> findUnreadMessagesInChat(@Param("chat") Chat chat, @Param("user") User user);
}
