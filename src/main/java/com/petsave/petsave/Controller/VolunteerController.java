package com.petsave.petsave.Controller;

import com.petsave.petsave.Service.VolunteerService;
import com.petsave.petsave.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/volunteers")
@RequiredArgsConstructor
public class VolunteerController {

    private final VolunteerService volunteerService;

    @GetMapping
    public ResponseEntity<List<VolunteerResponse>> listApprovedVolunteers() {
        return ResponseEntity.ok(volunteerService.listApprovedVolunteers());
    }

    @GetMapping("/my")
    public ResponseEntity<VolunteerResponse> getMyVolunteerProfile() {
        return ResponseEntity.ok(volunteerService.getMyVolunteerProfile());
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<VolunteerTaskResponse>> listMyTasks() {
        return ResponseEntity.ok(volunteerService.listMyTasks());
    }

    @GetMapping("/stats")
    public ResponseEntity<VolunteerStatsResponse> getStats() {
        return ResponseEntity.ok(volunteerService.getStats());
    }

    @GetMapping("/admin")
    public ResponseEntity<List<VolunteerResponse>> listForAdmin(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(volunteerService.listForAdmin(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VolunteerResponse> getVolunteer(@PathVariable Long id) {
        return ResponseEntity.ok(volunteerService.getVolunteer(id));
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<List<VolunteerTaskResponse>> listTasksForVolunteer(@PathVariable Long id) {
        return ResponseEntity.ok(volunteerService.listTasksForVolunteer(id));
    }

    @PostMapping
    public ResponseEntity<VolunteerResponse> registerVolunteer(@Valid @RequestBody VolunteerRequest request) {
        return ResponseEntity.ok(volunteerService.registerVolunteer(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VolunteerResponse> updateVolunteerProfile(@PathVariable Long id, @Valid @RequestBody VolunteerRequest request) {
        return ResponseEntity.ok(volunteerService.updateVolunteerProfile(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<VolunteerResponse> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(volunteerService.updateStatus(id, body.get("status")));
    }

    @PostMapping("/tasks")
    public ResponseEntity<VolunteerTaskResponse> createTask(@Valid @RequestBody VolunteerTaskRequest request) {
        return ResponseEntity.ok(volunteerService.createTask(request));
    }

    @PatchMapping("/tasks/{id}/complete")
    public ResponseEntity<VolunteerTaskResponse> completeTask(@PathVariable Long id, @Valid @RequestBody VolunteerTaskCompleteRequest request) {
        return ResponseEntity.ok(volunteerService.completeTask(id, request.getHoursSpent()));
    }
}
