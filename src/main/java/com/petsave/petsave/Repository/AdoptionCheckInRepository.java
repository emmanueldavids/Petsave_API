package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.AdoptionCheckIn;
import com.petsave.petsave.Entity.AdoptionCheckInMilestone;
import com.petsave.petsave.Entity.AdoptionCheckInStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdoptionCheckInRepository extends JpaRepository<AdoptionCheckIn, Long> {

    List<AdoptionCheckIn> findByAdoptionId(Long adoptionId);

    List<AdoptionCheckIn> findByAdoptionIdOrderByDueDateAsc(Long adoptionId);

    Optional<AdoptionCheckIn> findByAdoptionIdAndMilestone(Long adoptionId, AdoptionCheckInMilestone milestone);

    List<AdoptionCheckIn> findByStatus(AdoptionCheckInStatus status);

    @Query("SELECT c FROM AdoptionCheckIn c WHERE c.status = 'PENDING' AND c.dueDate <= :now")
    List<AdoptionCheckIn> findOverdueCheckIns(@Param("now") LocalDateTime now);

    @Query("SELECT c FROM AdoptionCheckIn c WHERE c.status = 'PENDING' AND c.dueDate <= :cutoffDate")
    List<AdoptionCheckIn> findPendingCheckInsDueBy(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT c FROM AdoptionCheckIn c WHERE c.status = 'PENDING' AND c.dueDate <= :alertDate ORDER BY c.dueDate ASC")
    List<AdoptionCheckIn> findCheckInsForAdminAlert(@Param("alertDate") LocalDateTime alertDate);

    List<AdoptionCheckIn> findByAdoptionIdAndStatus(Long adoptionId, AdoptionCheckInStatus status);

    Long countByAdoptionIdAndStatus(Long adoptionId, AdoptionCheckInStatus status);
}
