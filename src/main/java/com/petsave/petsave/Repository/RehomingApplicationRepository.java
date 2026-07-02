package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.PetRehoming;
import com.petsave.petsave.Entity.RehomingApplication;
import com.petsave.petsave.Entity.RehomingApplicationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RehomingApplicationRepository extends JpaRepository<RehomingApplication, Long> {

    @Override
    @EntityGraph(attributePaths = {"applicant", "rehoming"})
    Optional<RehomingApplication> findById(Long id);

    @EntityGraph(attributePaths = {"applicant", "rehoming"})
    List<RehomingApplication> findByRehomingOrderByCreatedAtDesc(PetRehoming rehoming);

    List<RehomingApplication> findByApplicantIdOrderByCreatedAtDesc(Long applicantId);

    @EntityGraph(attributePaths = {"applicant", "rehoming"})
    Optional<RehomingApplication> findByRehomingAndApplicant(PetRehoming rehoming, com.petsave.petsave.Entity.User applicant);

    long countByRehomingAndStatus(PetRehoming rehoming, RehomingApplicationStatus status);
}
