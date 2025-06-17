package com.petsave.petsave.Service;

import com.petsave.petsave.Entity.Donation;
import com.petsave.petsave.Repository.DonationRepository;
import com.petsave.petsave.dto.DonationRequest;
import com.petsave.petsave.dto.DonationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DonationService {

    private final DonationRepository donationRepository;

    public DonationService(DonationRepository donationRepository) {
        this.donationRepository = donationRepository;
    }

    public Page<DonationResponse> getAllDonations(String donorName, Pageable pageable) {
        Page<Donation> donations;

        if (donorName != null && !donorName.isEmpty()) {
            donations = donationRepository.findByDonorNameContainingIgnoreCase(donorName, pageable);
        } else {
            donations = donationRepository.findAll(pageable);
        }

        return donations.map(this::mapToResponse);
    }

    public DonationResponse createDonation(DonationRequest request) {
        Donation donation = new Donation();
        donation.setDonorName(request.getDonorName());
        donation.setDate(request.getDate());
        donation.setAmount(request.getAmount());
        donation.setGender(request.getGender());
        donation.setCountry(request.getCountry());

        Donation saved = donationRepository.save(donation);
        return mapToResponse(saved);
    }

    public DonationResponse getDonationById(Long id) {
        Optional<Donation> donation = donationRepository.findById(id);
        return donation.map(this::mapToResponse).orElse(null);
    }

    public DonationResponse updateDonation(Long id, DonationRequest request) {
        Optional<Donation> optional = donationRepository.findById(id);
        if (optional.isPresent()) {
            Donation donation = optional.get();
            donation.setDonorName(request.getDonorName());
            donation.setDate(request.getDate());
            donation.setAmount(request.getAmount());
            donation.setGender(request.getGender());
            donation.setCountry(request.getCountry());

            Donation updated = donationRepository.save(donation);
            return mapToResponse(updated);
        }
        return null;
    }

    public boolean deleteDonation(Long id) {
        if (donationRepository.existsById(id)) {
            donationRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Double getTotalDonations() {
        return donationRepository.findAll()
                .stream()
                .mapToDouble(Donation::getAmount)
                .sum();
    }

    public Long getDonationCount() {
        return donationRepository.count();
    }

    private DonationResponse mapToResponse(Donation donation) {
        DonationResponse response = new DonationResponse();
        response.setId(donation.getId());
        response.setDonorName(donation.getDonorName());
        response.setAmount(donation.getAmount());
        response.setDate(donation.getDate());
        response.setGender(donation.getGender());
        response.setCountry(donation.getCountry());
        return response;
    }
}
