package com.enterprisebank.audit.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuditEventResponse(
        Long id,
        String eventId,
        String transactionReference,
        String transactionType,
        Long sourceAccountId,
        Long destinationAccountId,
        BigDecimal amount,
        String currency,
        String description,
        Long initiatedByUserId,
        LocalDateTime transactionCompletedAt,
        LocalDateTime auditedAt
) {
}