package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.Adoption;
import com.petsave.petsave.Entity.AdoptionStatus;
import com.petsave.petsave.Entity.PetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdoptionRepository extends JpaRepository<Adoption, Long> {
    
    List<Adoption> findByUser_Id(Long userId);
    
    Page<Adoption> findByStatus(AdoptionStatus status, Pageable pageable);
    
    Page<Adoption> findByPetType(PetType petType, Pageable pageable);
    
    Page<Adoption> findByStatusAndPetType(AdoptionStatus status, PetType petType, Pageable pageable);
    
    @Query("SELECT a FROM Adoption a WHERE a.petName LIKE %:petName% AND (:status IS NULL OR a.status = :status) AND (:petType IS NULL OR a.petType = :petType)")
    Page<Adoption> searchAdoptions(@Param("petName") String petName, @Param("status") AdoptionStatus status, @Param("petType") PetType petType, Pageable pageable);
    
    @Query("SELECT a FROM Adoption a WHERE a.adopterEmail = :email")
    Optional<Adoption> findByAdopterEmail(@Param("email") String email);
    
    @Query("SELECT COUNT(a) FROM Adoption a WHERE a.status = :status")
    Long countByStatus(@Param("status") AdoptionStatus status);
    
    @Query("SELECT COUNT(a) FROM Adoption a WHERE a.petType = :petType")
    Long countByPetType(@Param("petType") PetType petType);
}
