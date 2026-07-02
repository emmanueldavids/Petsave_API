package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.PetRehoming;
import com.petsave.petsave.Entity.RehomingStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PetRehomingRepository extends JpaRepository<PetRehoming, Long> {

    @Override
    @EntityGraph(attributePaths = {"owner", "approvedBy", "adoptedBy"})
    Optional<PetRehoming> findById(Long id);

    @EntityGraph(attributePaths = {"owner", "approvedBy", "adoptedBy"})
    List<PetRehoming> findByStatusOrderByCreatedAtDesc(RehomingStatus status);

    @EntityGraph(attributePaths = {"owner", "approvedBy", "adoptedBy"})
    List<PetRehoming> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
}
