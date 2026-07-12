package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.PetSitter;
import com.petsave.petsave.Entity.PetSitterStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PetSitterRepository extends JpaRepository<PetSitter, Long> {

    @Override
    @EntityGraph(attributePaths = {"user"})
    Optional<PetSitter> findById(Long id);

    @EntityGraph(attributePaths = {"user"})
    Optional<PetSitter> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<PetSitter> findByStatus(PetSitterStatus status);

    @EntityGraph(attributePaths = {"user"})
    List<PetSitter> findByStatusAndCityIgnoreCase(PetSitterStatus status, String city);
}
