package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.PetRehoming;
import com.petsave.petsave.Entity.RehomingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetRehomingRepository extends JpaRepository<PetRehoming, Long> {
    List<PetRehoming> findByStatusOrderByCreatedAtDesc(RehomingStatus status);
    List<PetRehoming> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
}
