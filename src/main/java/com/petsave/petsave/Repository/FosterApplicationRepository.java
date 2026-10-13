package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.FosterApplication;
import com.petsave.petsave.Entity.FosterApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FosterApplicationRepository extends JpaRepository<FosterApplication, Long> {

    List<FosterApplication> findAllByOrderByCreatedAtDesc();

    List<FosterApplication> findByStatusOrderByCreatedAtDesc(FosterApplicationStatus status);

    List<FosterApplication> findByApplicantIdOrderByCreatedAtDesc(Long applicantId);

    List<FosterApplication> findByPetIdOrderByCreatedAtDesc(Long petId);

    boolean existsByPetIdAndApplicantIdAndStatusIn(Long petId, Long applicantId, List<FosterApplicationStatus> statuses);
}
