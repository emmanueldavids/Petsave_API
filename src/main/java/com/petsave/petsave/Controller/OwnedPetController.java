package com.petsave.petsave.Controller;

import com.petsave.petsave.Service.OwnedPetService;
import com.petsave.petsave.dto.OwnedPetRequest;
import com.petsave.petsave.dto.OwnedPetResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/my-pets")
@RequiredArgsConstructor
public class OwnedPetController {

    private final OwnedPetService ownedPetService;

    @GetMapping
    public ResponseEntity<List<OwnedPetResponse>> listMyPets() {
        return ResponseEntity.ok(ownedPetService.listMyPets());
    }

    @PostMapping
    public ResponseEntity<OwnedPetResponse> createPet(@Valid @RequestBody OwnedPetRequest request) {
        return ResponseEntity.ok(ownedPetService.createPet(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OwnedPetResponse> updatePet(@PathVariable Long id, @Valid @RequestBody OwnedPetRequest request) {
        return ResponseEntity.ok(ownedPetService.updatePet(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePet(@PathVariable Long id) {
        ownedPetService.deletePet(id);
        return ResponseEntity.ok(Map.of("message", "Pet deleted successfully"));
    }
}
