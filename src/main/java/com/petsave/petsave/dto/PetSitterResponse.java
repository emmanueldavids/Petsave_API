package com.petsave.petsave.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PetSitterResponse {

    private Long id;

    private UserResponse user;

    private String bio;

    private String profileImageUrl;

    private Integer serviceRadius;

    private String location;

    private String city;

    private Set<String> acceptedPetTypes;

    private BigDecimal ratePerDay;

    private BigDecimal ratePerNight;

    private Integer maxPetsAtOnce;

    private String status;

    private Float averageRating;

    private Integer totalReviews;

    // Only populated for the sitter themselves or an admin
    private String bankCode;
    private String accountNumber;
    private String accountName;
    private Boolean payoutDetailsOnFile;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Only populated by GET /api/sitters/{id} (single sitter detail), not the list endpoint
    private List<PetSittingReviewResponse> reviews;
}
