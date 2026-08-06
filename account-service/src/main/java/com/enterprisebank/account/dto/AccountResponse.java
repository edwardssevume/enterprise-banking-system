package com.enterprisebank.account.dto;

import com.enterprisebank.account.entity.AccountStatus;
import com.enterprisebank.account.entity.AccountType;
import com.enterprisebank.account.entity.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountResponse {

    private final Long id;
    private final String accountNumber;
    private final Long customerId;
    private final AccountType accountType;
    private final CurrencyCode currency;
    private final BigDecimal ledgerBalance;
    private final BigDecimal availableBalance;
    private final AccountStatus status;
    private final Long version;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public AccountResponse(
            Long id,
            String accountNumber,
            Long customerId,
            AccountType accountType,
            CurrencyCode currency,
            BigDecimal ledgerBalance,
            BigDecimal availableBalance,
            AccountStatus status,
            Long version,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.accountType = accountType;
        this.currency = currency;
        this.ledgerBalance = ledgerBalance;
        this.availableBalance = availableBalance;
        this.status = status;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public CurrencyCode getCurrency() {
        return currency;
    }

    public BigDecimal getLedgerBalance() {
        return ledgerBalance;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Long getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}