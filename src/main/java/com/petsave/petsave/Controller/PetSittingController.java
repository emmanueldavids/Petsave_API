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
import java.util.Map;

@RestController
@RequestMapping("/api/pet-sittings")
@RequiredArgsConstructor
public class PetSittingController {

    private final PetSittingService petSittingService;

    @PostMapping("/book")
    public ResponseEntity<PetSittingResponse> bookSitting(@Valid @RequestBody BookSittingRequest request) {
        return ResponseEntity.ok(petSittingService.bookSitting(request));
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<PetSittingResponse> acceptBooking(@PathVariable Long id) {
        return ResponseEntity.ok(petSittingService.acceptBooking(id));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<PetSittingResponse> rejectBooking(@PathVariable Long id) {
        return ResponseEntity.ok(petSittingService.rejectBooking(id));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<PetSittingResponse> payForBooking(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String callbackUrl = body != null ? body.get("callbackUrl") : null;
        return ResponseEntity.ok(petSittingService.payForBooking(id, callbackUrl));
    }

    @PostMapping("/{id}/verify-payment")
    public ResponseEntity<PetSittingResponse> verifyPayment(@PathVariable Long id) {
        return ResponseEntity.ok(petSittingService.verifyPayment(id));
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
    public ResponseEntity<PetSittingResponse> completeBooking(@PathVariable Long id) {
        return ResponseEntity.ok(petSittingService.completeBooking(id));
    }

    @PostMapping("/{id}/retry-payout")
    public ResponseEntity<PetSittingResponse> retryPayout(@PathVariable Long id) {
        return ResponseEntity.ok(petSittingService.retryPayout(id));
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<PetSittingReviewResponse> leaveReview(@PathVariable Long id, @Valid @RequestBody PetSittingReviewRequest request) {
        return ResponseEntity.ok(petSittingService.leaveReview(id, request));
    }

    @PatchMapping("/{id}/notes")
    public ResponseEntity<PetSittingResponse> updateNotes(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(petSittingService.updateNotes(id, body.get("notes")));
    }

    @PatchMapping("/{id}/dispute")
    public ResponseEntity<PetSittingResponse> raiseDispute(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(petSittingService.raiseDispute(id, body.get("reason")));
    }

    @PatchMapping("/{id}/resolve-dispute")
    public ResponseEntity<PetSittingResponse> resolveDispute(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(petSittingService.resolveDispute(id, body.get("resolution")));
    }
}
