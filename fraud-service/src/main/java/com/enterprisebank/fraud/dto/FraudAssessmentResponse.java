package com.enterprisebank.fraud.dto;

import com.enterprisebank.fraud.entity.ReviewStatus;
import com.enterprisebank.fraud.entity.RiskLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FraudAssessmentResponse(
        Long id,
        String eventId,
        String transactionReference,
        String transactionType,
        Long sourceAccountId,
        Long destinationAccountId,
        BigDecimal amount,
        String currency,
        Long initiatedByUserId,
        RiskLevel riskLevel,
        Integer riskScore,
        String fraudReason,
        ReviewStatus reviewStatus,
        LocalDateTime transactionCompletedAt,
        LocalDateTime assessedAt,

        Long reviewedByUserId,
        LocalDateTime reviewedAt,
        String reviewNotes
) {
}