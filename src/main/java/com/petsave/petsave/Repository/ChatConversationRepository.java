package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.ChatConversation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    @Override
    @EntityGraph(attributePaths = {"participant1", "participant2"})
    Optional<ChatConversation> findById(Long id);

    @EntityGraph(attributePaths = {"participant1", "participant2"})
    @Query("SELECT c FROM ChatConversation c WHERE c.participant1.id = :userId OR c.participant2.id = :userId " +
            "ORDER BY c.lastMessageAt DESC NULLS LAST, c.createdAt DESC")
    List<ChatConversation> findByParticipant(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"participant1", "participant2"})
    @Query("SELECT c FROM ChatConversation c WHERE " +
            "(c.participant1.id = :userId1 AND c.participant2.id = :userId2) OR " +
            "(c.participant1.id = :userId2 AND c.participant2.id = :userId1)")
    Optional<ChatConversation> findBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
