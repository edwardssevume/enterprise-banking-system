package com.enterprisebank.notification.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record NotificationResponse(
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
        String notificationMessage,
        LocalDateTime transactionCompletedAt,
        LocalDateTime createdAt
) {
}