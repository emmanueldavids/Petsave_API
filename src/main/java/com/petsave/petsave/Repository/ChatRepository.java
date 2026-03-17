package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.Chat;
import com.petsave.petsave.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    
    Optional<Chat> findByUser1AndUser2(User user1, User user2);
    
    @Query("SELECT c FROM Chat c WHERE (c.user1 = :user1 AND c.user2 = :user2) OR (c.user1 = :user2 AND c.user2 = :user1)")
    Optional<Chat> findByUser1AndUser2OrUser2AndUser1(@Param("user1") User user1, @Param("user2") User user2);
    
    List<Chat> findByUser1OrUser2(User user1, User user2);
    
    @Query("SELECT c FROM Chat c WHERE (c.user1 = :user OR c.user2 = :user) AND c.isActive = true ORDER BY c.updatedAt DESC")
    List<Chat> findActiveChatsByUser(@Param("user") User user);
    
    @Query("SELECT c FROM Chat c WHERE (c.user1 = :user OR c.user2 = :user) AND c.isActive = true AND " +
           "(LOWER(c.lastMessage) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.user1.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.user2.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Chat> searchChatsByUser(@Param("user") User user, @Param("search") String search);
    
    @Query("SELECT COUNT(c) FROM Chat c WHERE (c.user1 = :user OR c.user2 = :user) AND c.isActive = true")
    Long countActiveChatsByUser(@Param("user") User user);
    
    @Query("SELECT c FROM Chat c WHERE (c.user1 = :user OR c.user2 = :user) AND c.isActive = true AND " +
           "(c.user1UnreadCount > 0 OR c.user2UnreadCount > 0)")
    List<Chat> findChatsWithUnreadMessages(@Param("user") User user);
    
    @Query("SELECT c FROM Chat c WHERE (c.user1 = :user OR c.user2 = :user) AND c.isActive = true AND " +
           "CASE WHEN c.user1 = :user THEN c.user1UnreadCount > 0 ELSE c.user2UnreadCount > 0 END")
    List<Chat> findChatsWithUnreadMessagesForUser(@Param("user") User user);
}
