package com.petsave.petsave.Controller;

import com.petsave.petsave.Service.LostFoundPetService;
import com.petsave.petsave.dto.LostFoundPetRequest;
import com.petsave.petsave.dto.LostFoundPetResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lost-found")
@RequiredArgsConstructor
public class LostFoundController {

    private final LostFoundPetService lostFoundPetService;

    @GetMapping
    public ResponseEntity<List<LostFoundPetResponse>> listActive(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(lostFoundPetService.listActive(city, type));
    }

    @GetMapping("/my")
    public ResponseEntity<List<LostFoundPetResponse>> listMyReports() {
        return ResponseEntity.ok(lostFoundPetService.listMyReports());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LostFoundPetResponse> getReport(@PathVariable Long id) {
        return ResponseEntity.ok(lostFoundPetService.getReport(id));
    }

    @PostMapping
    public ResponseEntity<LostFoundPetResponse> createReport(@Valid @RequestBody LostFoundPetRequest request) {
        return ResponseEntity.ok(lostFoundPetService.createReport(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LostFoundPetResponse> updateReport(@PathVariable Long id, @Valid @RequestBody LostFoundPetRequest request) {
        return ResponseEntity.ok(lostFoundPetService.updateReport(id, request));
    }

    @PatchMapping("/{id}/reunited")
    public ResponseEntity<LostFoundPetResponse> markReunited(@PathVariable Long id) {
        return ResponseEntity.ok(lostFoundPetService.markReunited(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReport(@PathVariable Long id) {
        lostFoundPetService.deleteReport(id);
        return ResponseEntity.ok(Map.of("message", "Report deleted successfully"));
    }
}
