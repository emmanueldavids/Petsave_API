package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.LostFoundPet;
import com.petsave.petsave.Entity.LostFoundReportType;
import com.petsave.petsave.Entity.LostFoundStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LostFoundPetRepository extends JpaRepository<LostFoundPet, Long> {

    List<LostFoundPet> findByStatusOrderByCreatedAtDesc(LostFoundStatus status);

    List<LostFoundPet> findByStatusAndCityIgnoreCaseOrderByCreatedAtDesc(LostFoundStatus status, String city);

    List<LostFoundPet> findByStatusAndReportTypeOrderByCreatedAtDesc(LostFoundStatus status, LostFoundReportType reportType);

    List<LostFoundPet> findByStatusAndCityIgnoreCaseAndReportTypeOrderByCreatedAtDesc(
            LostFoundStatus status, String city, LostFoundReportType reportType);

    List<LostFoundPet> findByReporterIdOrderByCreatedAtDesc(Long reporterId);

    List<LostFoundPet> findByStatusAndExpiresAtBefore(LostFoundStatus status, LocalDateTime cutoff);
}
