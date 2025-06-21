// package com.petsave.petsave.Service;

// import java.time.LocalDateTime;

// import org.springframework.beans.factory.annotation.Autowired;

// import com.petsave.petsave.Entity.Donation;
// import com.petsave.petsave.Entity.UserDonation;
// import com.petsave.petsave.Repository.UserDonationRepository;
// import com.petsave.petsave.dto.DonationRequest;
// import com.petsave.petsave.dto.DonationResponse;

// public class UserDonationService {
    
    
//     @Autowired
//     private UserDonationRepository userDonationRepository;

//     public DonationResponse createDonation(DonationRequest donationRequest) {
//         String userId = donationRequest.getUserId(); // This should be extracted from token ideally
//         Float amount = donationRequest.getAmount();
//         Float donateamount = donationRequest.getAmount(); // Assuming this is the amount to be donated
       

//             // Update donation history
//             UserDonation userDonation = userDonationRepository.findById(userId).orElseGet(() -> {
//                 UserDonation newUserDonation = new UserDonation();
//                 newUserDonation.setUserId(userId);
//                 newUserDonation.setBalance(amount);
//                 newUserDonation.setDonatedAmount(donateamount);
//                 newUserDonation.setPreviousBalance(0.0f);
//                 return newUserDonation;
//             });
//         if (amount <= 0) {
//             throw new IllegalArgumentException("Donation amount must be greater than zero");
//         }
//         if (userDonation == null) {
//             throw new IllegalArgumentException("User not found or not authorized to donate");
//         }
//         // Update user's donation balance


//         userDonation.setPreviousBalance(userDonation.getCurrentBalance());
//         userDonation.setCurrentBalance(userDonation.getCurrentBalance() + amount);
//         userDonation.setTotalDonated(userDonation.getTotalDonated() + amount);

//         userDonationRepository.save(userDonation);

//         // Save the donation record
//         Donation donation = new Donation(); // Your donation entity
//         donation.setUserId(userId);
//         donation.setAmount(amount);
//         donation.setDate(LocalDateTime.now());
//         // Set other fields...

//         donationRepository.save(donation);

//         return DonationResponse.fromEntity(donation); // convert to DTO
//     }
// }