package com.petsave.petsave.Controller;

import com.petsave.petsave.Service.PetSittingService;
import com.petsave.petsave.dto.BookSittingRequest;
import com.petsave.petsave.dto.PetSittingResponse;
import com.petsave.petsave.dto.PetSittingReviewRequest;
import com.petsave.petsave.dto.PetSittingReviewResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pet-sittings")
@RequiredArgsConstructor
public class PetSittingController {

    private final PetSittingService petSittingService;

    @PostMapping("/book")
    public ResponseEntity<PetSittingResponse> bookSitting(@Valid @RequestBody BookSittingRequest request) {
        return ResponseEntity.ok(petSittingService.bookSitting(request));
    }

    @GetMapping
    public ResponseEntity<List<PetSittingResponse>> listMyBookings() {
        return ResponseEntity.ok(petSittingService.listMyBookings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetSittingResponse> getBooking(@PathVariable Long id) {
        return ResponseEntity.ok(petSittingService.getBooking(id));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<PetSittingResponse> confirmBooking(@PathVariable Long id) {
        return ResponseEntity.ok(petSittingService.confirmBooking(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PetSittingResponse> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.ok(petSittingService.cancelBooking(id));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<PetSittingResponse> adminCompleteBooking(@PathVariable Long id) {
        return ResponseEntity.ok(petSittingService.adminCompleteBooking(id));
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<PetSittingReviewResponse> leaveReview(@PathVariable Long id, @Valid @RequestBody PetSittingReviewRequest request) {
        return ResponseEntity.ok(petSittingService.leaveReview(id, request));
    }
}
