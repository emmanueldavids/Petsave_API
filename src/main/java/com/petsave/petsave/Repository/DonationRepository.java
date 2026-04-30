package com.petsave.petsave.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.petsave.petsave.Entity.Donation;
import com.petsave.petsave.Entity.PaymentStatus;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DonationRepository extends JpaRepository< Donation, Long> {
    Page<Donation> findByDonorNameContainingIgnoreCase(String donorName, Pageable pageable);
    Optional<Donation> findByReference(String reference);
    List<Donation> findByUserEmail(String email);
    List<Donation> findByEmailOrderByDateDesc(String email);
    
    @Query("SELECT COALESCE(SUM(d.amount), 0.0) FROM Donation d WHERE d.paymentStatus = :status AND d.email = :email")
    Double getTotalAmountByUser(@Param("status") PaymentStatus status, @Param("email") String email);
    
    @Query("SELECT COUNT(d) FROM Donation d WHERE d.paymentStatus = :status AND d.email = :email")
    Long getDonationCountByUser(@Param("status") PaymentStatus status, @Param("email") String email);
    
    @Query("SELECT COALESCE(SUM(d.amount), 0.0) FROM Donation d WHERE d.paymentStatus = :status")
    Double getTotalAmount(@Param("status") PaymentStatus status);
    
    @Query("SELECT COALESCE(SUM(d.amount), 0.0) FROM Donation d")
    Double getTotalAmountAll();
    
    // List<Donation> findByUsername(String username);
}