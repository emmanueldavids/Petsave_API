package com.petsave.petsave.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.petsave.petsave.Entity.Donation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DonationRepository extends JpaRepository< Donation, Long> {
    Page<Donation> findByDonorNameContainingIgnoreCase(String donorName, Pageable pageable);
}