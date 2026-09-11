package com.enterprisebank.fraud.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionCompletedEvent(
        String eventId,
        String transactionReference,
        String transactionType,
        Long sourceAccountId,
        Long destinationAccountId,
        BigDecimal amount,
        String currency,
        String description,
        Long initiatedByUserId,
        LocalDateTime completedAt
) {
}