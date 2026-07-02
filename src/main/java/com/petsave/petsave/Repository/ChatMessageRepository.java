package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.ChatConversation;
import com.petsave.petsave.Entity.ChatMessage;
import com.petsave.petsave.Entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @EntityGraph(attributePaths = {"sender"})
    List<ChatMessage> findByConversationOrderByCreatedAtAsc(ChatConversation conversation);

    long countByConversationAndSenderNotAndIsReadFalse(ChatConversation conversation, User excludeSender);

    List<ChatMessage> findByConversationAndSenderNotAndIsReadFalse(ChatConversation conversation, User excludeSender);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.sender.id <> :userId AND m.isRead = false AND " +
            "(m.conversation.participant1.id = :userId OR m.conversation.participant2.id = :userId)")
    long countUnreadForUser(@Param("userId") Long userId);
}
