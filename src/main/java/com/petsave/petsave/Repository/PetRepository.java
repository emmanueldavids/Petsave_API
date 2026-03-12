package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.Pet;
import com.petsave.petsave.Entity.PetStatus;
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
public interface PetRepository extends JpaRepository<Pet, Long> {
    
    // Find pets by status
    List<Pet> findByStatus(PetStatus status);
    
    // Find pets by type
    List<Pet> findByType(PetType type);
    
    // Find pets by availability
    List<Pet> findByAvailableTrue();
    
    // Find pets by location
    List<Pet> findByLocationContainingIgnoreCase(String location);
    
    // Find pets by status and availability
    Page<Pet> findByStatusAndAvailableTrue(PetStatus status, Pageable pageable);
    
    // Find pets by type and availability
    Page<Pet> findByTypeAndAvailableTrue(PetType type, Pageable pageable);
    
    // Find pets by name (case insensitive)
    List<Pet> findByNameContainingIgnoreCase(String name);
    
    // Find pets by breed (case insensitive)
    List<Pet> findByBreedContainingIgnoreCase(String breed);
    
    // Search pets with multiple criteria
    @Query("SELECT p FROM Pet p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER('%' || :name || '%')) AND " +
           "(:type IS NULL OR p.type = :type) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:available IS NULL OR p.available = :available)")
    Page<Pet> searchPets(@Param("name") String name,
                         @Param("type") PetType type,
                         @Param("status") PetStatus status,
                         @Param("available") Boolean available,
                         @Param("location") String location,
                         Pageable pageable);
    
    // Find pets uploaded by specific user
    List<Pet> findByUploadedBy(String uploadedBy);
    
    // Find pets with special needs
    List<Pet> findBySpecialNeedsIsNotNullAndSpecialNeedsNot(String specialNeeds);
    
    // Find senior pets (age > 7)
    @Query("SELECT p FROM Pet p WHERE p.age > 7 AND p.available = true")
    List<Pet> findSeniorPets();
    
    // Find pets requiring experienced owners
    List<Pet> findByRequiresExperiencedTrueAndAvailableTrue();
    
    // Count pets by status
    @Query("SELECT COUNT(p) FROM Pet p WHERE p.status = :status")
    Long countByStatus(@Param("status") PetStatus status);
    
    // Count pets by type
    @Query("SELECT COUNT(p) FROM Pet p WHERE p.type = :type")
    Long countByType(@Param("type") PetType type);
    
    // Find recently rescued pets (last 30 days)
    @Query(value = "SELECT p FROM Pet p WHERE p.rescueDate >= CURRENT_DATE - INTERVAL '30' DAY ORDER BY p.rescueDate DESC", nativeQuery = true)
    List<Pet> findRecentlyRescued();
    
    // Find popular pets (by view count)
    @Query("SELECT p FROM Pet p ORDER BY p.viewCount DESC")
    List<Pet> findPopularPets();
    
    // Find pets by adoption fee range
    @Query("SELECT p FROM Pet p WHERE p.adoptionFee BETWEEN :minFee AND :maxFee AND p.available = true")
    List<Pet> findByAdoptionFeeRange(@Param("minFee") Double minFee, @Param("maxFee") Double maxFee);
    
    // Find vaccinated and neutered pets
    List<Pet> findByVaccinatedTrueAndNeuteredTrueAndAvailableTrue();
    
    // Find pets good with specific criteria
    @Query("SELECT p FROM Pet p WHERE LOWER(p.goodWith) LIKE LOWER(CONCAT('%', :criteria, '%')) AND p.available = true")
    List<Pet> findGoodWith(@Param("criteria") String criteria);
}
