package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.Volunteer;
import com.petsave.petsave.Entity.VolunteerTask;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VolunteerTaskRepository extends JpaRepository<VolunteerTask, Long> {

    @Override
    @EntityGraph(attributePaths = {"volunteer", "volunteer.user", "pet"})
    Optional<VolunteerTask> findById(Long id);

    @EntityGraph(attributePaths = {"volunteer", "volunteer.user", "pet"})
    List<VolunteerTask> findByVolunteerOrderByCreatedAtDesc(Volunteer volunteer);
}
