package com.petsave.petsave.Service;

import com.petsave.petsave.Entity.Adoption;
import com.petsave.petsave.Entity.AdoptionStatus;
import com.petsave.petsave.Entity.PetType;
import com.petsave.petsave.Entity.User;
import com.petsave.petsave.Repository.AdoptionRepository;
import com.petsave.petsave.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdoptionService {

    private final AdoptionRepository adoptionRepository;
    private final UserRepository userRepository;

    public Adoption createAdoption(Adoption adoption, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        adoption.setUser(user);
        adoption.setStatus(AdoptionStatus.PENDING);
        
        return adoptionRepository.save(adoption);
    }

    public Page<Adoption> getAllAdoptions(Pageable pageable) {
        return adoptionRepository.findAll(pageable);
    }

    public Optional<Adoption> getAdoptionById(Long id) {
        return adoptionRepository.findById(id);
    }

    public List<Adoption> getAdoptionsByUserId(Long userId) {
        return adoptionRepository.findByUser_Id(userId);
    }

    public Page<Adoption> getAdoptionsByStatus(AdoptionStatus status, Pageable pageable) {
        return adoptionRepository.findByStatus(status, pageable);
    }

    public Page<Adoption> getAdoptionsByPetType(PetType petType, Pageable pageable) {
        return adoptionRepository.findByPetType(petType, pageable);
    }

    public Page<Adoption> searchAdoptions(String petName, AdoptionStatus status, PetType petType, Pageable pageable) {
        if (petName != null && !petName.trim().isEmpty()) {
            return adoptionRepository.searchAdoptions(petName, status, petType, pageable);
        } else if (status != null && petType != null) {
            return adoptionRepository.findByStatusAndPetType(status, petType, pageable);
        } else if (status != null) {
            return adoptionRepository.findByStatus(status, pageable);
        } else if (petType != null) {
            return adoptionRepository.findByPetType(petType, pageable);
        }
        return adoptionRepository.findAll(pageable);
    }

    public Adoption updateAdoptionStatus(Long id, AdoptionStatus status) {
        Adoption adoption = adoptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Adoption not found with id: " + id));
        
        adoption.setStatus(status);
        return adoptionRepository.save(adoption);
    }

    public Adoption updateAdoption(Long id, Adoption adoptionDetails) {
        Adoption adoption = adoptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Adoption not found with id: " + id));

        adoption.setPetName(adoptionDetails.getPetName());
        adoption.setPetBreed(adoptionDetails.getPetBreed());
        adoption.setPetAge(adoptionDetails.getPetAge());
        adoption.setPetDescription(adoptionDetails.getPetDescription());
        adoption.setPetImageUrl(adoptionDetails.getPetImageUrl());
        adoption.setPetType(adoptionDetails.getPetType());
        adoption.setAdopterName(adoptionDetails.getAdopterName());
        adoption.setAdopterEmail(adoptionDetails.getAdopterEmail());
        adoption.setAdopterPhone(adoptionDetails.getAdopterPhone());
        adoption.setAdopterAddress(adoptionDetails.getAdopterAddress());
        adoption.setApplicationReason(adoptionDetails.getApplicationReason());

        return adoptionRepository.save(adoption);
    }

    public void deleteAdoption(Long id) {
        if (!adoptionRepository.existsById(id)) {
            throw new RuntimeException("Adoption not found with id: " + id);
        }
        adoptionRepository.deleteById(id);
    }

    public Long getAdoptionCount() {
        return adoptionRepository.count();
    }

    public Long getAdoptionCountByStatus(AdoptionStatus status) {
        return adoptionRepository.countByStatus(status);
    }

    public Long getAdoptionCountByPetType(PetType petType) {
        return adoptionRepository.countByPetType(petType);
    }

    public Optional<Adoption> getAdoptionByAdopterEmail(String email) {
        return adoptionRepository.findByAdopterEmail(email);
    }
}
