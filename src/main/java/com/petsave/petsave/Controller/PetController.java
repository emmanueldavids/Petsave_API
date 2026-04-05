package com.petsave.petsave.Controller;

import com.petsave.petsave.Entity.Pet;
import com.petsave.petsave.Entity.PetStatus;
import com.petsave.petsave.Entity.PetType;
import com.petsave.petsave.Repository.PetRepository;
import com.petsave.petsave.Service.PetService;
import com.petsave.petsave.dto.PetRequest;
import com.petsave.petsave.dto.PetStatistics;
import com.petsave.petsave.dto.PetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://127.0.0.1:3000", "http://127.0.0.1:3001"}, allowCredentials = "true")
public class PetController {

    private final PetService petService;
    private final PetRepository petRepository;

    // Create a new pet
    @PostMapping
    public ResponseEntity<Pet> createPet(@RequestBody PetRequest request) {
        Pet pet = convertToEntity(request);
        Pet savedPet = petService.createPet(pet);
        return ResponseEntity.ok(savedPet);
    }

    // Create a new pet with image upload
    @PostMapping(value = "/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Pet> createPetWithImage(
            @RequestParam String name,
            @RequestParam String breed,
            @RequestParam Integer age,
            @RequestParam String description,
            @RequestParam String type,
            @RequestParam String status,
            @RequestParam String location,
            @RequestParam Boolean available,
            @RequestParam String uploadedBy,
            @RequestParam String rescueDate,
            @RequestParam Double adoptionFee,
            @RequestParam Boolean vaccinated,
            @RequestParam Boolean neutered,
            @RequestParam Boolean houseTrained,
            @RequestParam Boolean requiresExperienced,
            @RequestParam String adoptionRequirements,
            @RequestParam(required = false) MultipartFile image) {
        
        PetRequest request = new PetRequest();
        request.setName(name);
        request.setBreed(breed);
        request.setAge(age);
        request.setDescription(description);
        request.setType(PetType.valueOf(type));
        request.setStatus(PetStatus.valueOf(status));
        request.setLocation(location);
        request.setAvailable(available);
        request.setUploadedBy(uploadedBy);
        request.setRescueDate(rescueDate);
        request.setAdoptionFee(adoptionFee);
        request.setVaccinated(vaccinated);
        request.setNeutered(neutered);
        request.setHouseTrained(houseTrained);
        request.setRequiresExperienced(requiresExperienced);
        request.setAdoptionRequirements(adoptionRequirements);
        request.setImage(image);
        
        Pet pet = convertToEntity(request);
        Pet savedPet = petService.createPetWithImage(pet, image);
        return ResponseEntity.ok(savedPet);
    }

    // Get all pets with pagination
    @GetMapping
    public ResponseEntity<Page<Pet>> getAllPets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) PetType type,
            @RequestParam(required = false) PetStatus status,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) String location) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Pet> pets;
        
        if (name != null || type != null || status != null || available != null || location != null) {
            pets = petService.searchPets(name, type, status, available, location, pageable);
        } else {
            pets = petService.getAllPets(pageable);
        }
        return ResponseEntity.ok(pets);
    }

    // Get pet by ID
    @GetMapping("/{id}")
    public ResponseEntity<Pet> getPetById(@PathVariable Long id) {
        Optional<Pet> pet = petService.getPetById(id);
        return pet.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    // Update pet
    @PutMapping("/{id}")
    public ResponseEntity<Pet> updatePet(@PathVariable Long id, @RequestBody PetRequest request) {
        Pet petDetails = convertToEntity(request);
        try {
            Pet updatedPet = petService.updatePet(id, petDetails);
            return ResponseEntity.ok(updatedPet);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Update pet with image upload
    @PutMapping(value = "/{id}/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Pet> updatePetWithImage(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String breed,
            @RequestParam Integer age,
            @RequestParam String description,
            @RequestParam String type,
            @RequestParam String status,
            @RequestParam String location,
            @RequestParam Boolean available,
            @RequestParam String uploadedBy,
            @RequestParam String rescueDate,
            @RequestParam Double adoptionFee,
            @RequestParam Boolean vaccinated,
            @RequestParam Boolean neutered,
            @RequestParam Boolean houseTrained,
            @RequestParam Boolean requiresExperienced,
            @RequestParam String adoptionRequirements,
            @RequestParam(required = false) MultipartFile image) {
        
        PetRequest request = new PetRequest();
        request.setName(name);
        request.setBreed(breed);
        request.setAge(age);
        request.setDescription(description);
        request.setType(PetType.valueOf(type));
        request.setStatus(PetStatus.valueOf(status));
        request.setLocation(location);
        request.setAvailable(available);
        request.setUploadedBy(uploadedBy);
        request.setRescueDate(rescueDate);
        request.setAdoptionFee(adoptionFee);
        request.setVaccinated(vaccinated);
        request.setNeutered(neutered);
        request.setHouseTrained(houseTrained);
        request.setRequiresExperienced(requiresExperienced);
        request.setAdoptionRequirements(adoptionRequirements);
        request.setImage(image);
        
        Pet petDetails = convertToEntity(request);
        try {
            Pet updatedPet = petService.updatePetWithImage(id, petDetails, image);
            return ResponseEntity.ok(updatedPet);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete pet
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable Long id) {
        try {
            petService.deletePet(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Update pet status
    @PatchMapping("/{id}/status")
    public ResponseEntity<Pet> updatePetStatus(@PathVariable Long id, @RequestParam PetStatus status) {
        try {
            Pet updatedPet = petService.updatePetStatus(id, status);
            return ResponseEntity.ok(updatedPet);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Toggle pet availability
    @PatchMapping("/{id}/availability")
    public ResponseEntity<Pet> toggleAvailability(@PathVariable Long id) {
        try {
            Pet updatedPet = petService.toggleAvailability(id);
            return ResponseEntity.ok(updatedPet);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Increment favorite count
    @PatchMapping("/{id}/favorite")
    public ResponseEntity<Pet> incrementFavoriteCount(@PathVariable Long id) {
        try {
            Pet updatedPet = petService.incrementFavoriteCount(id);
            return ResponseEntity.ok(updatedPet);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get pets by status - for frontend sections (paginated version)
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<Pet>> getPetsByStatus(
            @PathVariable PetStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Pet> pets = petRepository.findByStatusAndAvailableTrue(status, pageable);
        return ResponseEntity.ok(pets);
    }

    // Get pets by type
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Pet>> getPetsByType(@PathVariable PetType type) {
        List<Pet> pets = petService.getPetsByType(type);
        return ResponseEntity.ok(pets);
    }

    // Get available pets
    @GetMapping("/available")
    public ResponseEntity<List<Pet>> getAvailablePets() {
        List<Pet> pets = petService.getAvailablePets();
        return ResponseEntity.ok(pets);
    }

    // Get pets by location
    @GetMapping("/location/{location}")
    public ResponseEntity<List<Pet>> getPetsByLocation(@PathVariable String location) {
        List<Pet> pets = petService.getPetsByLocation(location);
        return ResponseEntity.ok(pets);
    }

    // Get pets uploaded by user
    @GetMapping("/uploaded-by/{uploadedBy}")
    public ResponseEntity<List<Pet>> getPetsByUploadedBy(@PathVariable String uploadedBy) {
        List<Pet> pets = petService.getPetsByUploadedBy(uploadedBy);
        return ResponseEntity.ok(pets);
    }

    // Get recently rescued pets
    @GetMapping("/recently-rescued")
    public ResponseEntity<List<Pet>> getRecentlyRescuedPets() {
        List<Pet> pets = petService.getRecentlyRescuedPets();
        return ResponseEntity.ok(pets);
    }

    // Get popular pets
    @GetMapping("/popular")
    public ResponseEntity<List<Pet>> getPopularPets() {
        List<Pet> pets = petService.getPopularPets();
        return ResponseEntity.ok(pets);
    }

    // Get senior pets
    @GetMapping("/senior")
    public ResponseEntity<List<Pet>> getSeniorPets() {
        List<Pet> pets = petService.getSeniorPets();
        return ResponseEntity.ok(pets);
    }

    // Get pets requiring experienced owners
    @GetMapping("/requires-experienced")
    public ResponseEntity<List<Pet>> getPetsRequiringExperienced() {
        List<Pet> pets = petService.getPetsRequiringExperienced();
        return ResponseEntity.ok(pets);
    }

    // Get pets with special needs
    @GetMapping("/special-needs")
    public ResponseEntity<List<Pet>> getPetsWithSpecialNeeds() {
        List<Pet> pets = petService.getPetsWithSpecialNeeds();
        return ResponseEntity.ok(pets);
    }

    // Get pets by adoption fee range
    @GetMapping("/adoption-fee")
    public ResponseEntity<List<Pet>> getPetsByAdoptionFeeRange(
            @RequestParam Double minFee,
            @RequestParam Double maxFee) {
        List<Pet> pets = petService.getPetsByAdoptionFeeRange(minFee, maxFee);
        return ResponseEntity.ok(pets);
    }

    // Get vaccinated and neutered pets
    @GetMapping("/vaccinated-neutered")
    public ResponseEntity<List<Pet>> getVaccinatedAndNeuteredPets() {
        List<Pet> pets = petService.getVaccinatedAndNeuteredPets();
        return ResponseEntity.ok(pets);
    }

    // Get pets good with specific criteria
    @GetMapping("/good-with")
    public ResponseEntity<List<Pet>> getPetsGoodWith(@RequestParam String criteria) {
        List<Pet> pets = petService.getPetsGoodWith(criteria);
        return ResponseEntity.ok(pets);
    }

    // Get statistics
    @GetMapping("/statistics")
    public ResponseEntity<PetStatistics> getStatistics() {
        PetStatistics statistics = petService.getStatistics();
        return ResponseEntity.ok(statistics);
    }

    // Get adoptable pets (FOR_ADOPTION status)
    @GetMapping("/adoptable")
    public ResponseEntity<Page<Pet>> getAdoptablePets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return getPetsByStatus(PetStatus.FOR_ADOPTION, page, size);
    }

    // Get donation pets (RESCUED_DONATION status)
    @GetMapping("/donation-pets")
    public ResponseEntity<Page<Pet>> getDonationPets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return getPetsByStatus(PetStatus.RESCUED_DONATION, page, size);
    }

    // Get pets in treatment (IN_TREATMENT status)
    @GetMapping("/treatment")
    public ResponseEntity<Page<Pet>> getTreatmentPets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return getPetsByStatus(PetStatus.IN_TREATMENT, page, size);
    }

    // Get adopted pets (ADOPTED status)
    @GetMapping("/adopted")
    public ResponseEntity<Page<Pet>> getAdoptedPets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return getPetsByStatus(PetStatus.ADOPTED, page, size);
    }

    // Helper method to convert DTO to Entity
    private Pet convertToEntity(PetRequest request) {
        Pet pet = new Pet();
        pet.setName(request.getName());
        pet.setBreed(request.getBreed());
        pet.setAge(request.getAge());
        pet.setDescription(request.getDescription());
        pet.setImageUrl(request.getImageUrl());
        pet.setType(request.getType());
        pet.setStatus(request.getStatus());
        pet.setLocation(request.getLocation());
        pet.setMedicalHistory(request.getMedicalHistory());
        pet.setSpecialNeeds(request.getSpecialNeeds());
        pet.setAvailable(request.getAvailable());
        pet.setUploadedBy(request.getUploadedBy());
        pet.setRescueDate(request.getRescueDate());
        pet.setAdoptionDate(request.getAdoptionDate());
        pet.setAdoptionFee(request.getAdoptionFee());
        pet.setStory(request.getStory());
        pet.setVaccinated(request.getVaccinated());
        pet.setNeutered(request.getNeutered());
        pet.setHouseTrained(request.getHouseTrained());
        pet.setTemperament(request.getTemperament());
        pet.setGoodWith(request.getGoodWith());
        pet.setWeight(request.getWeight());
        pet.setColor(request.getColor());
        pet.setMicrochipId(request.getMicrochipId());
        pet.setRequiresExperienced(request.getRequiresExperienced());
        pet.setAdoptionRequirements(request.getAdoptionRequirements());
        return pet;
    }
}
