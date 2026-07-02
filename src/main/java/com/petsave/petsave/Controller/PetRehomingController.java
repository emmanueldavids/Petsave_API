package com.petsave.petsave.Controller;

import com.petsave.petsave.Entity.PetRehoming;
import com.petsave.petsave.Entity.RehomingApplication;
import com.petsave.petsave.Entity.RehomingStatus;
import com.petsave.petsave.Service.PetRehomingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rehomings")
@RequiredArgsConstructor
public class PetRehomingController {

    private final PetRehomingService petRehomingService;

    @GetMapping
    public List<PetRehoming> listApprovedRehomings() {
        return petRehomingService.listApprovedRehomings();
    }

    @GetMapping("/my")
    public ResponseEntity<List<PetRehoming>> getMyRehomings() {
        return ResponseEntity.ok(petRehomingService.listMyRehomings());
    }

    @GetMapping("/admin")
    public ResponseEntity<List<PetRehoming>> listForAdmin(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(petRehomingService.listForAdmin(status));
    }

    @GetMapping("/my-applications")
    public ResponseEntity<List<RehomingApplication>> listMyApplications() {
        return ResponseEntity.ok(petRehomingService.listMyApplications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetRehoming> getRehoming(@PathVariable Long id) {
        return petRehomingService.getRehoming(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PetRehoming> createRehoming(@RequestBody PetRehoming rehoming) {
        return ResponseEntity.ok(petRehomingService.createRehoming(rehoming));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetRehoming> updateRehoming(@PathVariable Long id, @RequestBody PetRehoming updates) {
        return ResponseEntity.ok(petRehomingService.updateRehoming(id, updates));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> withdrawRehoming(@PathVariable Long id) {
        petRehomingService.withdrawRehoming(id);
        return ResponseEntity.ok(Map.of("message", "Rehoming withdrawn successfully"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PetRehoming> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        RehomingStatus status = RehomingStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(petRehomingService.updateStatus(id, status));
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<RehomingApplication> apply(@PathVariable Long id) {
        return ResponseEntity.ok(petRehomingService.applyForRehoming(id));
    }

    @GetMapping("/{id}/applications")
    public ResponseEntity<List<RehomingApplication>> listApplications(@PathVariable Long id) {
        return ResponseEntity.ok(petRehomingService.listApplications(id));
    }

    @PatchMapping("/{id}/approve/{applicationId}")
    public ResponseEntity<RehomingApplication> approveApplicant(@PathVariable Long id, @PathVariable Long applicationId) {
        return ResponseEntity.ok(petRehomingService.approveApplicant(id, applicationId));
    }
}
