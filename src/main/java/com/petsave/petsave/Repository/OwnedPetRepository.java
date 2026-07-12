package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.OwnedPet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OwnedPetRepository extends JpaRepository<OwnedPet, Long> {

    @Override
    @EntityGraph(attributePaths = {"owner"})
    Optional<OwnedPet> findById(Long id);

    @EntityGraph(attributePaths = {"owner"})
    List<OwnedPet> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
}
