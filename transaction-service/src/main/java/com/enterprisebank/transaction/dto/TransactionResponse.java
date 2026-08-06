package com.enterprisebank.transaction.dto;

import com.enterprisebank.transaction.entity.TransactionDirection;
import com.enterprisebank.transaction.entity.TransactionStatus;
import com.enterprisebank.transaction.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private final Long id;
    private final String transactionReference;
    private final String idempotencyKey;
    private final TransactionType transactionType;
    private final TransactionStatus status;
    private final TransactionDirection direction;
    private final Long sourceAccountId;
    private final Long destinationAccountId;
    private final BigDecimal amount;
    private final String currency;
    private final String description;
    private final Long initiatedByUserId;
    private final String failureReason;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime completedAt;

    public TransactionResponse(
            Long id,
            String transactionReference,
            String idempotencyKey,
            TransactionType transactionType,
            TransactionStatus status,
            TransactionDirection direction,
            Long sourceAccountId,
            Long destinationAccountId,
            BigDecimal amount,
            String currency,
            String description,
            Long initiatedByUserId,
            String failureReason,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime completedAt
    ) {
        this.id = id;
        this.transactionReference = transactionReference;
        this.idempotencyKey = idempotencyKey;
        this.transactionType = transactionType;
        this.status = status;
        this.direction = direction;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.initiatedByUserId = initiatedByUserId;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public TransactionDirection getDirection() {
        return direction;
    }

    public Long getSourceAccountId() {
        return sourceAccountId;
    }

    public Long getDestinationAccountId() {
        return destinationAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }

    public Long getInitiatedByUserId() {
        return initiatedByUserId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}