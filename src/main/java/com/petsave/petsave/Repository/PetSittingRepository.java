package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.PetSitting;
import com.petsave.petsave.Entity.PetSittingStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PetSittingRepository extends JpaRepository<PetSitting, Long> {

    @Override
    @EntityGraph(attributePaths = {"owner", "sitter", "pet"})
    Optional<PetSitting> findById(Long id);

    @EntityGraph(attributePaths = {"owner", "sitter", "pet"})
    Optional<PetSitting> findByPaymentReference(String paymentReference);

    @EntityGraph(attributePaths = {"owner", "sitter", "pet"})
    @Query("SELECT b FROM PetSitting b WHERE b.owner.id = :userId OR b.sitter.id = :userId ORDER BY b.createdAt DESC")
    List<PetSitting> findByParticipant(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"owner", "sitter", "pet"})
    List<PetSitting> findByStatusInAndEndDateBefore(List<PetSittingStatus> statuses, LocalDateTime cutoff);
}
