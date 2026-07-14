package com.petsave.petsave.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pet_sitters")
@Data
@NoArgsConstructor
public class PetSitter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 2000)
    private String bio;

    private String profileImageUrl;

    private Integer serviceRadius;

    private String location;

    private String city;

    @ElementCollection
    @CollectionTable(name = "pet_sitter_accepted_types", joinColumns = @JoinColumn(name = "pet_sitter_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "pet_type")
    private Set<SitterPetType> acceptedPetTypes = new HashSet<>();

    @Column(precision = 10, scale = 2)
    private BigDecimal ratePerDay;

    @Column(precision = 10, scale = 2)
    private BigDecimal ratePerNight;

    private Integer maxPetsAtOnce;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetSitterStatus status = PetSitterStatus.PENDING;

    @Column(nullable = false)
    private Float averageRating = 0f;

    @Column(nullable = false)
    private Integer totalReviews = 0;

    // Bank details for payout via Paystack Transfer — optional at registration,
    // required before any payout can actually be attempted.
    private String bankCode;
    private String accountNumber;
    private String accountName;

    // Paystack recipient type ("nuban" for Nigerian banks, "mobile_money" for Ghana MTN/Vodafone/AirtelTigo,
    // etc.) and settlement currency ("NGN", "GHS", ...) — these vary by sitter's country, so they can't be
    // hardcoded. Defaults preserve prior behavior for sitters who registered before this field existed.
    private String recipientType = "nuban";
    private String payoutCurrency = "NGN";

    @Column(name = "paystack_recipient_code")
    private String paystackRecipientCode;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
