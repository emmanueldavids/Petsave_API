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

    @Query("SELECT COUNT(b) FROM PetSitting b WHERE b.sitter.id = :sitterId AND b.status IN :statuses " +
            "AND b.startDate < :endDate AND b.endDate > :startDate")
    long countOverlappingBookings(@Param("sitterId") Long sitterId,
                                   @Param("statuses") List<PetSittingStatus> statuses,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(b) FROM PetSitting b WHERE b.pet.id = :petId AND b.status IN :statuses " +
            "AND b.startDate < :endDate AND b.endDate > :startDate")
    long countOverlappingBookingsForPet(@Param("petId") Long petId,
                                         @Param("statuses") List<PetSittingStatus> statuses,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    boolean existsByPetId(Long petId);
}
