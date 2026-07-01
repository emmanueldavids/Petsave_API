package com.petsave.petsave.Service;

import com.petsave.petsave.Entity.PetRehoming;
import com.petsave.petsave.Entity.RehomingStatus;
import com.petsave.petsave.Entity.User;
import com.petsave.petsave.Repository.PetRehomingRepository;
import com.petsave.petsave.Repository.RehomingApplicationRepository;
import com.petsave.petsave.Repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetRehomingServiceTest {

    @Mock
    private PetRehomingRepository petRehomingRepository;

    @Mock
    private RehomingApplicationRepository rehomingApplicationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PetRehomingService petRehomingService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createRehoming_assignsOwnerAndPendingReviewStatus() {
        User owner = new User();
        owner.setId(7L);
        owner.setEmail("owner@example.com");
        owner.setRole("USER");

        PetRehoming rehoming = new PetRehoming();
        rehoming.setPetName("Buddy");
        rehoming.setPetType("DOG");
        rehoming.setPetBreed("Labrador");

        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(petRehomingRepository.save(any(PetRehoming.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(owner, null, "ROLE_USER")
        );

        PetRehoming saved = petRehomingService.createRehoming(rehoming);

        assertEquals(owner, saved.getOwner());
        assertEquals(RehomingStatus.PENDING_REVIEW, saved.getStatus());
    }
}
