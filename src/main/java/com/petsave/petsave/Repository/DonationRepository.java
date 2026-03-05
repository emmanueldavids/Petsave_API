package com.petsave.petsave.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.petsave.petsave.Entity.Donation;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DonationRepository extends JpaRepository< Donation, Long> {
    Page<Donation> findByDonorNameContainingIgnoreCase(String donorName, Pageable pageable);
    Optional<Donation> findByReference(String reference);
    List<Donation> findByUserEmail(String email);
    
    @Query("SELECT COALESCE(SUM(d.amount), 0.0) FROM Donation d WHERE d.paymentStatus = 'COMPLETED'")
    Double getTotalAmount();
    
    // List<Donation> findByUsername(String username);

}