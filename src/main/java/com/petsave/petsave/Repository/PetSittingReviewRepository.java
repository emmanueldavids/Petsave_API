package com.petsave.petsave.Repository;

import com.petsave.petsave.Entity.PetSitting;
import com.petsave.petsave.Entity.PetSittingReview;
import com.petsave.petsave.Entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetSittingReviewRepository extends JpaRepository<PetSittingReview, Long> {

    @EntityGraph(attributePaths = {"reviewer", "reviewee", "booking"})
    List<PetSittingReview> findByBookingOrderByCreatedAtDesc(PetSitting booking);

    @EntityGraph(attributePaths = {"reviewer", "reviewee", "booking"})
    List<PetSittingReview> findByRevieweeIdOrderByCreatedAtDesc(Long revieweeId);

    boolean existsByBookingAndReviewer(PetSitting booking, User reviewer);
}
