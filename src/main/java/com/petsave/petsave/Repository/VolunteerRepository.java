package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.BadgeLevel;
import com.petsave.petsave.Entity.Volunteer;
import com.petsave.petsave.Entity.VolunteerStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {

    @Override
    @EntityGraph(attributePaths = {"user"})
    Optional<Volunteer> findById(Long id);

    @EntityGraph(attributePaths = {"user"})
    Optional<Volunteer> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<Volunteer> findByStatusIn(List<VolunteerStatus> statuses);

    @EntityGraph(attributePaths = {"user"})
    List<Volunteer> findByStatusOrderByCreatedAtDesc(VolunteerStatus status);

    @EntityGraph(attributePaths = {"user"})
    List<Volunteer> findAllByOrderByCreatedAtDesc();

    long countByStatusIn(List<VolunteerStatus> statuses);

    long countByBadgeLevel(BadgeLevel badgeLevel);

    @Query("SELECT COALESCE(SUM(v.hoursLogged), 0) FROM Volunteer v")
    long sumHoursLogged();

    @Query("SELECT COALESCE(SUM(v.totalTasksCompleted), 0) FROM Volunteer v")
    long sumTasksCompleted();
}
