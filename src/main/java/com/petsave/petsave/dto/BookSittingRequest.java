package com.petsave.petsave.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookSittingRequest {

    @NotNull(message = "sitterId is required")
    private Long sitterId;

    @NotNull(message = "petId is required")
    private Long petId;

    @NotNull(message = "startDate is required")
    private LocalDateTime startDate;

    @NotNull(message = "endDate is required")
    private LocalDateTime endDate;

    private String ownerNotes;

    private String callbackUrl;
}
