package com.petsave.petsave.Service;

import com.petsave.petsave.Entity.Pet;
import com.petsave.petsave.Entity.PetStatus;
import com.petsave.petsave.Entity.PetType;
import com.petsave.petsave.Repository.PetRepository;
import com.petsave.petsave.dto.PetStatistics;
import com.petsave.petsave.dto.PetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PetService {

    private final PetRepository petRepository;
    private final String uploadDir = "uploads/pet-images/";

    // Create a new pet
    public Pet createPet(Pet pet) {
        log.info("Creating new pet: {}", pet.getName());
        
        // Set default values
        if (pet.getViewCount() == null) {
            pet.setViewCount(0L);
        }
        if (pet.getFavoriteCount() == null) {
            pet.setFavoriteCount(0L);
        }
        if (pet.getRescueDate() == null) {
            pet.setRescueDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        
        Pet savedPet = petRepository.save(pet);
        log.info("Pet created successfully with ID: {}", savedPet.getId());
        return savedPet;
    }

    // Create a new pet with image
    public Pet createPetWithImage(Pet pet, MultipartFile image) {
        log.info("Creating new pet with image: {}", pet.getName());
        
        // Set default values
        if (pet.getViewCount() == null) {
            pet.setViewCount(0L);
        }
        if (pet.getFavoriteCount() == null) {
            pet.setFavoriteCount(0L);
        }
        if (pet.getRescueDate() == null) {
            pet.setRescueDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        
        // Handle image upload
        if (image != null && !image.isEmpty()) {
            try {
                // Temporarily disabled - LOB fields removed from entity
                // pet.setImage(image.getBytes());
                // pet.setImageType(image.getContentType());
                
                // Also save to filesystem for serving
                String imagePath = saveImage(image);
                pet.setImageUrl(imagePath);
            } catch (IOException e) {
                log.error("Failed to process image: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to process image: " + e.getMessage(), e);
            }
        }
        
        Pet savedPet = petRepository.save(pet);
        log.info("Pet created successfully with ID: {}", savedPet.getId());
        return savedPet;
    }

    private String saveImage(MultipartFile image) throws IOException {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadPath.toAbsolutePath());
            }
            
            String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, image.getBytes());
            log.info("Saved image to: {}", filePath.toAbsolutePath());
            return "/uploads/pet-images/" + fileName; // returned as public URL path
        } catch (IOException e) {
            log.error("Failed to save image: {}", e.getMessage(), e);
            throw new IOException("Failed to save image file: " + e.getMessage(), e);
        }
    }

    // Helper method to convert Pet to PetResponse (excludes binary data)
    public PetResponse mapToResponse(Pet pet) {
        PetResponse response = new PetResponse();
        response.setId(pet.getId());
        response.setName(pet.getName());
        response.setBreed(pet.getBreed());
        response.setAge(pet.getAge());
        response.setDescription(pet.getDescription());
        response.setImageUrl(pet.getImageUrl());
        response.setType(pet.getType());
        response.setStatus(pet.getStatus());
        response.setLocation(pet.getLocation());
        response.setMedicalHistory(pet.getMedicalHistory());
        response.setSpecialNeeds(pet.getSpecialNeeds());
        response.setAvailable(pet.getAvailable());
        response.setUploadedBy(pet.getUploadedBy());
        response.setRescueDate(pet.getRescueDate());
        response.setAdoptionDate(pet.getAdoptionDate());
        response.setAdoptionFee(pet.getAdoptionFee());
        response.setStory(pet.getStory());
        response.setVaccinated(pet.getVaccinated());
        response.setNeutered(pet.getNeutered());
        response.setHouseTrained(pet.getHouseTrained());
        response.setTemperament(pet.getTemperament());
        response.setGoodWith(pet.getGoodWith());
        response.setWeight(pet.getWeight());
        response.setColor(pet.getColor());
        response.setMicrochipId(pet.getMicrochipId());
        response.setRequiresExperienced(pet.getRequiresExperienced());
        response.setAdoptionRequirements(pet.getAdoptionRequirements());
        response.setViewCount(pet.getViewCount());
        response.setFavoriteCount(pet.getFavoriteCount());
        return response;
    }

    // Update pet with image
    public Pet updatePetWithImage(Long id, Pet petDetails, MultipartFile image) {
        log.info("Updating pet with image: {}", id);
        
        Pet existingPet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pet not found with id: " + id));
        
        // Update all fields
        existingPet.setName(petDetails.getName());
        existingPet.setBreed(petDetails.getBreed());
        existingPet.setAge(petDetails.getAge());
        existingPet.setDescription(petDetails.getDescription());
        existingPet.setType(petDetails.getType());
        existingPet.setStatus(petDetails.getStatus());
        existingPet.setLocation(petDetails.getLocation());
        existingPet.setMedicalHistory(petDetails.getMedicalHistory());
        existingPet.setSpecialNeeds(petDetails.getSpecialNeeds());
        existingPet.setAvailable(petDetails.getAvailable());
        existingPet.setUploadedBy(petDetails.getUploadedBy());
        existingPet.setRescueDate(petDetails.getRescueDate());
        existingPet.setAdoptionDate(petDetails.getAdoptionDate());
        existingPet.setAdoptionFee(petDetails.getAdoptionFee());
        existingPet.setStory(petDetails.getStory());
        existingPet.setVaccinated(petDetails.getVaccinated());
        existingPet.setNeutered(petDetails.getNeutered());
        existingPet.setHouseTrained(petDetails.getHouseTrained());
        existingPet.setTemperament(petDetails.getTemperament());
        existingPet.setGoodWith(petDetails.getGoodWith());
        existingPet.setWeight(petDetails.getWeight());
        existingPet.setColor(petDetails.getColor());
        existingPet.setMicrochipId(petDetails.getMicrochipId());
        existingPet.setRequiresExperienced(petDetails.getRequiresExperienced());
        existingPet.setAdoptionRequirements(petDetails.getAdoptionRequirements());
        
        // Handle image upload
        if (image != null && !image.isEmpty()) {
            try {
                // Temporarily disabled - LOB fields removed from entity
                // existingPet.setImage(image.getBytes());
                // existingPet.setImageType(image.getContentType());
                
                // Also save new image to filesystem
                String imagePath = saveImage(image);
                existingPet.setImageUrl(imagePath);
            } catch (IOException e) {
                log.error("Failed to process image: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to process image: " + e.getMessage(), e);
            }
        }
        
        Pet savedPet = petRepository.save(existingPet);
        log.info("Pet updated successfully with ID: {}", savedPet.getId());
        return savedPet;
    }

    // Get all pets with pagination
    public Page<Pet> getAllPets(Pageable pageable) {
        return petRepository.findAll(pageable);
    }

    // Get pet by ID
    public Optional<Pet> getPetById(Long id) {
        Optional<Pet> pet = petRepository.findById(id);
        if (pet.isPresent()) {
            // Increment view count
            Pet petEntity = pet.get();
            petEntity.setViewCount(petEntity.getViewCount() + 1);
            petRepository.save(petEntity);
        }
        return pet;
    }

    // Update pet
    public Pet updatePet(Long id, Pet petDetails) {
        log.info("Updating pet with ID: {}", id);
        
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pet not found with id: " + id));

        // Update all fields
        pet.setName(petDetails.getName());
        pet.setBreed(petDetails.getBreed());
        pet.setAge(petDetails.getAge());
        pet.setDescription(petDetails.getDescription());
        pet.setImageUrl(petDetails.getImageUrl());
        pet.setType(petDetails.getType());
        pet.setStatus(petDetails.getStatus());
        pet.setLocation(petDetails.getLocation());
        pet.setMedicalHistory(petDetails.getMedicalHistory());
        pet.setSpecialNeeds(petDetails.getSpecialNeeds());
        pet.setAvailable(petDetails.getAvailable());
        pet.setUploadedBy(petDetails.getUploadedBy());
        pet.setRescueDate(petDetails.getRescueDate());
        pet.setAdoptionDate(petDetails.getAdoptionDate());
        pet.setAdoptionFee(petDetails.getAdoptionFee());
        pet.setStory(petDetails.getStory());
        pet.setVaccinated(petDetails.getVaccinated());
        pet.setNeutered(petDetails.getNeutered());
        pet.setHouseTrained(petDetails.getHouseTrained());
        pet.setTemperament(petDetails.getTemperament());
        pet.setGoodWith(petDetails.getGoodWith());
        pet.setWeight(petDetails.getWeight());
        pet.setColor(petDetails.getColor());
        pet.setMicrochipId(petDetails.getMicrochipId());
        pet.setRequiresExperienced(petDetails.getRequiresExperienced());
        pet.setAdoptionRequirements(petDetails.getAdoptionRequirements());

        Pet updatedPet = petRepository.save(pet);
        log.info("Pet updated successfully: {}", updatedPet.getName());
        return updatedPet;
    }

    // Delete pet
    public void deletePet(Long id) {
        log.info("Deleting pet with ID: {}", id);
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pet not found with id: " + id));
        petRepository.delete(pet);
        log.info("Pet deleted successfully");
    }

    // Search pets
    public Page<Pet> searchPets(String name, PetType type, PetStatus status, Boolean available, String location, Pageable pageable) {
        return petRepository.searchPets(name, type, status, available, location, pageable);
    }

    // Get pets by status
    public List<Pet> getPetsByStatus(PetStatus status) {
        return petRepository.findByStatus(status);
    }

    // Get pets by type
    public List<Pet> getPetsByType(PetType type) {
        return petRepository.findByType(type);
    }

    // Get available pets
    public List<Pet> getAvailablePets() {
        return petRepository.findByAvailableTrue();
    }

    // Get pets by location
    public List<Pet> getPetsByLocation(String location) {
        return petRepository.findByLocationContainingIgnoreCase(location);
    }

    // Get pets uploaded by user
    public List<Pet> getPetsByUploadedBy(String uploadedBy) {
        return petRepository.findByUploadedBy(uploadedBy);
    }

    // Get recently rescued pets
    public List<Pet> getRecentlyRescuedPets() {
        return petRepository.findRecentlyRescued();
    }

    // Get popular pets
    public List<Pet> getPopularPets() {
        return petRepository.findPopularPets();
    }

    // Get senior pets
    public List<Pet> getSeniorPets() {
        return petRepository.findSeniorPets();
    }

    // Get pets requiring experienced owners
    public List<Pet> getPetsRequiringExperienced() {
        return petRepository.findByRequiresExperiencedTrueAndAvailableTrue();
    }

    // Get pets with special needs
    public List<Pet> getPetsWithSpecialNeeds() {
        return petRepository.findBySpecialNeedsIsNotNullAndSpecialNeedsNot("");
    }

    // Update pet status
    public Pet updatePetStatus(Long id, PetStatus status) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pet not found with id: " + id));
        
        pet.setStatus(status);
        
        // If pet is being adopted, set adoption date
        if (status == PetStatus.ADOPTED) {
            pet.setAvailable(false);
            pet.setAdoptionDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        
        return petRepository.save(pet);
    }

    // Toggle pet availability
    public Pet toggleAvailability(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pet not found with id: " + id));
        
        pet.setAvailable(!pet.getAvailable());
        return petRepository.save(pet);
    }

    // Increment favorite count
    public Pet incrementFavoriteCount(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pet not found with id: " + id));
        
        pet.setFavoriteCount(pet.getFavoriteCount() + 1);
        return petRepository.save(pet);
    }

    // Get statistics
    public PetStatistics getStatistics() {
        return PetStatistics.builder()
                .totalPets(petRepository.count())
                .availablePets((long) petRepository.findByAvailableTrue().size())
                .rescuedDonationPets(petRepository.countByStatus(PetStatus.RESCUED_DONATION))
                .forAdoptionPets(petRepository.countByStatus(PetStatus.FOR_ADOPTION))
                .adoptedPets(petRepository.countByStatus(PetStatus.ADOPTED))
                .inTreatmentPets(petRepository.countByStatus(PetStatus.IN_TREATMENT))
                .fosterCarePets(petRepository.countByStatus(PetStatus.FOSTER_CARE))
                .dogs(petRepository.countByType(PetType.DOG))
                .cats(petRepository.countByType(PetType.CAT))
                .others(petRepository.countByType(PetType.OTHER))
                .build();
    }

    // Get pets by adoption fee range
    public List<Pet> getPetsByAdoptionFeeRange(Double minFee, Double maxFee) {
        return petRepository.findByAdoptionFeeRange(minFee, maxFee);
    }

    // Get vaccinated and neutered pets
    public List<Pet> getVaccinatedAndNeuteredPets() {
        return petRepository.findByVaccinatedTrueAndNeuteredTrueAndAvailableTrue();
    }

    // Get pets good with specific criteria
    public List<Pet> getPetsGoodWith(String criteria) {
        return petRepository.findGoodWith(criteria);
    }
}
