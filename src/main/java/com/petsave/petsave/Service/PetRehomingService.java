package com.petsave.petsave.Service;

import com.petsave.petsave.Entity.*;
import com.petsave.petsave.Repository.PetRehomingRepository;
import com.petsave.petsave.Repository.RehomingApplicationRepository;
import com.petsave.petsave.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PetRehomingService {

    private final PetRehomingRepository petRehomingRepository;
    private final RehomingApplicationRepository rehomingApplicationRepository;
    private final UserRepository userRepository;

    public PetRehoming createRehoming(PetRehoming rehoming) {
        User owner = getCurrentUser();
        rehoming.setOwner(owner);
        rehoming.setStatus(RehomingStatus.PENDING_REVIEW);
        return petRehomingRepository.save(rehoming);
    }

    public List<PetRehoming> listApprovedRehomings() {
        return petRehomingRepository.findByStatusOrderByCreatedAtDesc(RehomingStatus.APPROVED);
    }

    public List<PetRehoming> listMyRehomings() {
        User currentUser = getCurrentUser();
        return petRehomingRepository.findByOwnerIdOrderByCreatedAtDesc(currentUser.getId());
    }

    public Optional<PetRehoming> getRehoming(Long id) {
        return petRehomingRepository.findById(id);
    }

    public PetRehoming updateRehoming(Long id, PetRehoming updates) {
        PetRehoming existing = petRehomingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rehoming not found with id: " + id));

        User currentUser = getCurrentUser();
        if (!existing.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the owner can update this listing");
        }

        if (updates.getPetName() != null) existing.setPetName(updates.getPetName());
        if (updates.getPetType() != null) existing.setPetType(updates.getPetType());
        if (updates.getPetBreed() != null) existing.setPetBreed(updates.getPetBreed());
        if (updates.getPetAge() != null) existing.setPetAge(updates.getPetAge());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getImageUrl() != null) existing.setImageUrl(updates.getImageUrl());
        if (updates.getReason() != null) existing.setReason(updates.getReason());
        if (updates.getMedicalHistory() != null) existing.setMedicalHistory(updates.getMedicalHistory());
        if (updates.getVaccinated() != null) existing.setVaccinated(updates.getVaccinated());
        if (updates.getNeutered() != null) existing.setNeutered(updates.getNeutered());
        if (updates.getTemperament() != null) existing.setTemperament(updates.getTemperament());
        if (updates.getGoodWith() != null) existing.setGoodWith(updates.getGoodWith());
        if (updates.getSpecialNeeds() != null) existing.setSpecialNeeds(updates.getSpecialNeeds());

        return petRehomingRepository.save(existing);
    }

    public void withdrawRehoming(Long id) {
        PetRehoming existing = petRehomingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rehoming not found with id: " + id));
        User currentUser = getCurrentUser();
        if (!existing.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the owner can withdraw this listing");
        }
        existing.setStatus(RehomingStatus.WITHDRAWN);
        petRehomingRepository.save(existing);
    }

    public PetRehoming updateStatus(Long id, RehomingStatus status) {
        User currentUser = getCurrentUser();
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new RuntimeException("Only admins can change rehoming status");
        }

        PetRehoming existing = petRehomingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rehoming not found with id: " + id));
        existing.setStatus(status);
        existing.setApprovedBy(currentUser);
        return petRehomingRepository.save(existing);
    }

    public RehomingApplication applyForRehoming(Long rehomingId) {
        PetRehoming rehoming = petRehomingRepository.findById(rehomingId)
                .orElseThrow(() -> new RuntimeException("Rehoming not found with id: " + rehomingId));
        User applicant = getCurrentUser();

        if (rehoming.getOwner().getId().equals(applicant.getId())) {
            throw new RuntimeException("You cannot apply to your own rehoming listing");
        }

        RehomingApplication existing = rehomingApplicationRepository.findByRehomingAndApplicant(rehoming, applicant).orElse(null);
        if (existing != null) {
            return existing;
        }

        RehomingApplication application = new RehomingApplication();
        application.setRehoming(rehoming);
        application.setApplicant(applicant);
        application.setStatus(RehomingApplicationStatus.PENDING);
        return rehomingApplicationRepository.save(application);
    }

    public List<RehomingApplication> listApplications(Long rehomingId) {
        PetRehoming rehoming = petRehomingRepository.findById(rehomingId)
                .orElseThrow(() -> new RuntimeException("Rehoming not found with id: " + rehomingId));
        User currentUser = getCurrentUser();
        if (!rehoming.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the owner can view applications for this listing");
        }
        return rehomingApplicationRepository.findByRehomingOrderByCreatedAtDesc(rehoming);
    }

    public RehomingApplication approveApplicant(Long rehomingId, Long applicantId) {
        PetRehoming rehoming = petRehomingRepository.findById(rehomingId)
                .orElseThrow(() -> new RuntimeException("Rehoming not found with id: " + rehomingId));
        User currentUser = getCurrentUser();
        if (!rehoming.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the owner can approve an applicant");
        }

        RehomingApplication application = rehomingApplicationRepository.findById(applicantId)
                .orElseThrow(() -> new RuntimeException("Application not found with id: " + applicantId));
        if (!application.getRehoming().getId().equals(rehoming.getId())) {
            throw new RuntimeException("Application does not belong to this rehoming listing");
        }

        application.setStatus(RehomingApplicationStatus.APPROVED);
        rehoming.setStatus(RehomingStatus.ADOPTED);
        rehoming.setAdoptedBy(application.getApplicant());
        petRehomingRepository.save(rehoming);
        return rehomingApplicationRepository.save(application);
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
