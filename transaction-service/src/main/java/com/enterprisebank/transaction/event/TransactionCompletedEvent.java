package com.enterprisebank.transaction.event;

import com.enterprisebank.transaction.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionCompletedEvent(
        String eventId,
        String transactionReference,
        TransactionType transactionType,
        Long sourceAccountId,
        Long destinationAccountId,
        BigDecimal amount,
        String currency,
        String description,
        Long initiatedByUserId,
        LocalDateTime completedAt
) {
}