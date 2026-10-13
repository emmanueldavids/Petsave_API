package com.petsave.petsave.Service;

import com.petsave.petsave.Entity.OwnedPet;
import com.petsave.petsave.Entity.SitterPetType;
import com.petsave.petsave.Entity.User;
import com.petsave.petsave.Repository.OwnedPetRepository;
import com.petsave.petsave.Repository.PetSittingRepository;
import com.petsave.petsave.Repository.UserRepository;
import com.petsave.petsave.dto.OwnedPetRequest;
import com.petsave.petsave.dto.OwnedPetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OwnedPetService {

    private final OwnedPetRepository ownedPetRepository;
    private final PetSittingRepository petSittingRepository;
    private final UserRepository userRepository;

    public List<OwnedPetResponse> listMyPets() {
        User currentUser = getCurrentUser();
        return ownedPetRepository.findByOwnerIdOrderByCreatedAtDesc(currentUser.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OwnedPetResponse createPet(OwnedPetRequest request) {
        User currentUser = getCurrentUser();
        OwnedPet pet = new OwnedPet();
        pet.setOwner(currentUser);
        pet.setName(request.getName());
        pet.setPetType(parseEnum(request.getPetType()));
        pet.setBreed(request.getBreed());
        pet.setNotes(request.getNotes());

        OwnedPet saved = ownedPetRepository.save(pet);
        return mapToResponse(saved);
    }

    public OwnedPetResponse updatePet(Long id, OwnedPetRequest request) {
        User currentUser = getCurrentUser();
        OwnedPet pet = ownedPetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pet not found with id: " + id));
        if (!pet.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only update your own pet");
        }

        pet.setName(request.getName());
        pet.setPetType(parseEnum(request.getPetType()));
        pet.setBreed(request.getBreed());
        pet.setNotes(request.getNotes());

        OwnedPet saved = ownedPetRepository.save(pet);
        return mapToResponse(saved);
    }

    public void deletePet(Long id) {
        User currentUser = getCurrentUser();
        OwnedPet pet = ownedPetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pet not found with id: " + id));
        if (!pet.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only delete your own pet");
        }
        if (petSittingRepository.existsByPetId(pet.getId())) {
            throw new RuntimeException("This pet has sitting bookings on record and cannot be deleted");
        }
        ownedPetRepository.delete(pet);
    }

    private SitterPetType parseEnum(String value) {
        try {
            return SitterPetType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid petType: " + value);
        }
    }

    private OwnedPetResponse mapToResponse(OwnedPet pet) {
        return OwnedPetResponse.builder()
                .id(pet.getId())
                .name(pet.getName())
                .petType(pet.getPetType().name())
                .breed(pet.getBreed())
                .notes(pet.getNotes())
                .createdAt(pet.getCreatedAt())
                .build();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("Authentication required");
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
}
