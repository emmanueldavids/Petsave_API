package com.petsave.petsave.Controller;

import com.petsave.petsave.dto.AdoptionCheckInAlertResponse;
import com.petsave.petsave.dto.AdoptionCheckInResponse;
import com.petsave.petsave.dto.AdoptionCheckInSubmitRequest;
import com.petsave.petsave.Service.AdoptionCheckInService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/adoptions")
@RequiredArgsConstructor
@Slf4j
public class AdoptionCheckInController {

    private final AdoptionCheckInService checkInService;

    // ================= GET ALL CHECK-INS FOR AN ADOPTION =================
    @GetMapping("/{adoptionId}/check-ins")
    public ResponseEntity<Map<String, Object>> getCheckIns(@PathVariable Long adoptionId) {
        try {
            List<AdoptionCheckInResponse> checkIns = checkInService.getCheckInsByAdoption(adoptionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", checkIns);
            response.put("count", checkIns.size());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error fetching check-ins for adoption {}: {}", adoptionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // ================= GET SINGLE CHECK-IN DETAILS =================
    @GetMapping("/check-ins/{checkInId}")
    public ResponseEntity<Map<String, Object>> getCheckInDetails(@PathVariable Long checkInId) {
        try {
            AdoptionCheckInResponse checkIn = checkInService.getCheckInById(checkInId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", checkIn);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error fetching check-in {}: {}", checkInId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // ================= SUBMIT CHECK-IN (WITH OPTIONAL PHOTO) =================
    @PostMapping("/{adoptionId}/check-ins/{checkInId}/submit")
    public ResponseEntity<Map<String, Object>> submitCheckIn(
            @PathVariable Long adoptionId,
            @PathVariable Long checkInId,
            @ModelAttribute AdoptionCheckInSubmitRequest request,
            @RequestParam(value = "photo", required = false) MultipartFile photoFile) {
        try {
            AdoptionCheckInResponse response = checkInService.submitCheckIn(adoptionId, checkInId, request, photoFile);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Check-in submitted successfully");
            result.put("data", response);
            
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("Error uploading photo for check-in {}: {}", checkInId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Error uploading photo: " + e.getMessage()
            ));
        } catch (RuntimeException e) {
            log.error("Error submitting check-in {}: {}", checkInId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // ================= ADMIN: GET MISSED CHECK-IN ALERTS =================
    @GetMapping("/admin/check-ins/alerts")
    public ResponseEntity<Map<String, Object>> getAdminAlerts(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // TODO: Add role-based authorization check here
            List<AdoptionCheckInAlertResponse> alerts = checkInService.getAdminAlerts();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("alerts", alerts);
            response.put("count", alerts.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching admin alerts: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Error fetching alerts"
            ));
        }
    }

    // ================= GET CHECK-IN STATISTICS =================
    @GetMapping("/{adoptionId}/check-ins/stats")
    public ResponseEntity<Map<String, Object>> getCheckInStats(@PathVariable Long adoptionId) {
        try {
            long completed = checkInService.getCompletedCheckInsCount(adoptionId);
            long pending = checkInService.getPendingCheckInsCount(adoptionId);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("completed", completed);
            stats.put("pending", pending);
            stats.put("total", completed + pending);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching check-in stats for adoption {}: {}", adoptionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Error fetching statistics"
            ));
        }
    }
}
