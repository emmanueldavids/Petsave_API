package com.petsave.petsave.Controller;

import com.petsave.petsave.Entity.Adoption;
import com.petsave.petsave.Entity.AdoptionStatus;
import com.petsave.petsave.Entity.PetType;
import com.petsave.petsave.Service.AdoptionService;
import com.petsave.petsave.Service.PetService;
import com.petsave.petsave.dto.AdoptionRequest;
import com.petsave.petsave.dto.AdoptionResponse;
import com.petsave.petsave.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/adoptions")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"}, allowCredentials = "true")
public class AdoptionController {

    private final AdoptionService adoptionService;
    private final PetService petService;

    @PostMapping
    public ResponseEntity<AdoptionResponse> createAdoption(@RequestBody AdoptionRequest request) {
        // For now, we'll use a hardcoded user ID. In a real app, you'd get this from the JWT token
        Long userId = 1L; // This should be extracted from JWT token
        
        // Find the pet and link it to the adoption
        var pet = petService.getPetById(request.getPetId())
            .orElseThrow(() -> new RuntimeException("Pet not found with id: " + request.getPetId()));
        
        // Set the pet's image URL from the fetched pet
        request.setPetImageUrl(pet.getImageUrl());
        
        Adoption adoption = convertToEntity(request);
        adoption.setPet(pet); // Link the pet to the adoption
        
        Adoption savedAdoption = adoptionService.createAdoption(adoption, userId);
        
        return ResponseEntity.ok(convertToResponse(savedAdoption));
    }

    @GetMapping
    public ResponseEntity<Page<AdoptionResponse>> getAllAdoptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String petName,
            @RequestParam(required = false) AdoptionStatus status,
            @RequestParam(required = false) PetType petType) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Adoption> adoptions;
        
        if (petName != null || status != null || petType != null) {
            adoptions = adoptionService.searchAdoptions(petName, status, petType, pageable);
        } else {
            adoptions = adoptionService.getAllAdoptions(pageable);
        }
        
        Page<AdoptionResponse> response = adoptions.map(this::convertToResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdoptionResponse> getAdoptionById(@PathVariable Long id) {
        Optional<Adoption> adoption = adoptionService.getAdoptionById(id);
        return adoption.map(value -> ResponseEntity.ok(convertToResponse(value)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AdoptionResponse>> getAdoptionsByUserId(@PathVariable Long userId) {
        List<Adoption> adoptions = adoptionService.getAdoptionsByUserId(userId);
        List<AdoptionResponse> response = adoptions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<AdoptionResponse>> getAdoptionsByStatus(
            @PathVariable AdoptionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Adoption> adoptions = adoptionService.getAdoptionsByStatus(status, pageable);
        Page<AdoptionResponse> response = adoptions.map(this::convertToResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pet-type/{petType}")
    public ResponseEntity<Page<AdoptionResponse>> getAdoptionsByPetType(
            @PathVariable PetType petType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Adoption> adoptions = adoptionService.getAdoptionsByPetType(petType, pageable);
        Page<AdoptionResponse> response = adoptions.map(this::convertToResponse);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AdoptionResponse> updateAdoptionStatus(
            @PathVariable Long id,
            @RequestParam AdoptionStatus status) {
        Adoption adoption = adoptionService.updateAdoptionStatus(id, status);
        return ResponseEntity.ok(convertToResponse(adoption));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdoptionResponse> updateAdoption(
            @PathVariable Long id,
            @RequestBody AdoptionRequest request) {
        Adoption adoptionDetails = convertToEntity(request);
        Adoption adoption = adoptionService.updateAdoption(id, adoptionDetails);
        return ResponseEntity.ok(convertToResponse(adoption));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AdoptionResponse> patchAdoption(
            @PathVariable Long id,
            @RequestBody AdoptionRequest request) {
        Optional<Adoption> existingAdoption = adoptionService.getAdoptionById(id);
        if (existingAdoption.isPresent()) {
            Adoption adoption = existingAdoption.get();
            
            // Only update non-null fields
            if (request.getPetName() != null) {
                adoption.setPetName(request.getPetName());
            }
            if (request.getPetBreed() != null) {
                adoption.setPetBreed(request.getPetBreed());
            }
            if (request.getPetAge() != null) {
                adoption.setPetAge(request.getPetAge());
            }
            if (request.getPetDescription() != null) {
                adoption.setPetDescription(request.getPetDescription());
            }
            if (request.getPetImageUrl() != null) {
                adoption.setPetImageUrl(request.getPetImageUrl());
            }
            if (request.getPetType() != null) {
                adoption.setPetType(request.getPetType());
            }
            if (request.getAdopterName() != null) {
                adoption.setAdopterName(request.getAdopterName());
            }
            if (request.getAdopterEmail() != null) {
                adoption.setAdopterEmail(request.getAdopterEmail());
            }
            if (request.getAdopterPhone() != null) {
                adoption.setAdopterPhone(request.getAdopterPhone());
            }
            if (request.getAdopterAddress() != null) {
                adoption.setAdopterAddress(request.getAdopterAddress());
            }
            if (request.getApplicationReason() != null) {
                adoption.setApplicationReason(request.getApplicationReason());
            }
            
            Adoption updatedAdoption = adoptionService.updateAdoption(id, adoption);
            return ResponseEntity.ok(convertToResponse(updatedAdoption));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdoption(@PathVariable Long id) {
        adoptionService.deleteAdoption(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/total")
    public ResponseEntity<Long> getTotalAdoptions() {
        Long count = adoptionService.getAdoptionCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/status/{status}")
    public ResponseEntity<Long> getAdoptionsByStatusCount(@PathVariable AdoptionStatus status) {
        Long count = adoptionService.getAdoptionCountByStatus(status);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/pet-type/{petType}")
    public ResponseEntity<Long> getAdoptionsByPetTypeCount(@PathVariable PetType petType) {
        Long count = adoptionService.getAdoptionCountByPetType(petType);
        return ResponseEntity.ok(count);
    }

    private Adoption convertToEntity(AdoptionRequest request) {
        Adoption adoption = new Adoption();
        adoption.setPetName(request.getPetName());
        adoption.setPetBreed(request.getPetBreed());
        adoption.setPetAge(request.getPetAge());
        adoption.setPetDescription(request.getPetDescription());
        adoption.setPetImageUrl(request.getPetImageUrl());
        adoption.setPetType(request.getPetType());
        adoption.setAdopterName(request.getAdopterName());
        adoption.setAdopterEmail(request.getAdopterEmail());
        adoption.setAdopterPhone(request.getAdopterPhone());
        adoption.setAdopterAddress(request.getAdopterAddress());
        adoption.setApplicationReason(request.getApplicationReason());
        return adoption;
    }

    private AdoptionResponse convertToResponse(Adoption adoption) {
        AdoptionResponse response = new AdoptionResponse();
        response.setId(adoption.getId());
        response.setPetName(adoption.getPetName());
        response.setPetBreed(adoption.getPetBreed());
        response.setPetAge(adoption.getPetAge());
        response.setPetDescription(adoption.getPetDescription());
        response.setPetImageUrl(adoption.getPetImageUrl());
        response.setPetType(adoption.getPetType());
        response.setStatus(adoption.getStatus());
        response.setAdopterName(adoption.getAdopterName());
        response.setAdopterEmail(adoption.getAdopterEmail());
        response.setAdopterPhone(adoption.getAdopterPhone());
        response.setAdopterAddress(adoption.getAdopterAddress());
        response.setApplicationReason(adoption.getApplicationReason());
        response.setApplicationDate(adoption.getApplicationDate());
        response.setCreatedAt(adoption.getCreatedAt());
        response.setUpdatedAt(adoption.getUpdatedAt());
        
        if (adoption.getUser() != null) {
            UserDto userDto = new UserDto();
            userDto.setId(adoption.getUser().getId());
            userDto.setName(adoption.getUser().getName());
            userDto.setEmail(adoption.getUser().getEmail());
            userDto.setUsername(adoption.getUser().getUsername());
            response.setUser(userDto);
        }
        
        return response;
    }
}
