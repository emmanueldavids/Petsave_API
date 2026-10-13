package com.petsave.petsave.Controller;

import com.petsave.petsave.Service.FosterApplicationService;
import com.petsave.petsave.dto.FosterApplicationRequest;
import com.petsave.petsave.dto.FosterApplicationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/foster/applications")
@RequiredArgsConstructor
public class FosterApplicationController {

    private final FosterApplicationService fosterApplicationService;

    @GetMapping
    public ResponseEntity<List<FosterApplicationResponse>> listForAdmin(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(fosterApplicationService.listForAdmin(status));
    }

    @PostMapping
    public ResponseEntity<FosterApplicationResponse> applyToFoster(@Valid @RequestBody FosterApplicationRequest request) {
        return ResponseEntity.ok(fosterApplicationService.applyToFoster(request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<FosterApplicationResponse>> listMyApplications() {
        return ResponseEntity.ok(fosterApplicationService.listMyApplications());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<FosterApplicationResponse> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(fosterApplicationService.updateStatus(id, body.get("status"), body.get("adminNotes")));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<FosterApplicationResponse> completeFoster(@PathVariable Long id) {
        return ResponseEntity.ok(fosterApplicationService.completeFoster(id));
    }
}
