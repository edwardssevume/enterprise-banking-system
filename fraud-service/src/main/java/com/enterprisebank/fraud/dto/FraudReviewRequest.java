package com.enterprisebank.fraud.dto;

import com.enterprisebank.fraud.entity.ReviewStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FraudReviewRequest(

        @NotNull(message = "Review status is required")
        ReviewStatus reviewStatus,

        @Size(
                max = 1000,
                message = "Review notes cannot exceed 1000 characters"
        )
        String reviewNotes
) {
}