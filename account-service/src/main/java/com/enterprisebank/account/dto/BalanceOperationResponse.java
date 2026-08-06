package com.enterprisebank.account.dto;

import java.math.BigDecimal;

public class BalanceOperationResponse {

    private final Long accountId;
    private final String accountNumber;
    private final BigDecimal ledgerBalance;
    private final BigDecimal availableBalance;
    private final String currency;
    private final String transactionReference;
    private final String message;

    public BalanceOperationResponse(
            Long accountId,
            String accountNumber,
            BigDecimal ledgerBalance,
            BigDecimal availableBalance,
            String currency,
            String transactionReference,
            String message
    ) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.ledgerBalance = ledgerBalance;
        this.availableBalance = availableBalance;
        this.currency = currency;
        this.transactionReference = transactionReference;
        this.message = message;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getLedgerBalance() {
        return ledgerBalance;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public String getCurrency() {
        return currency;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public String getMessage() {
        return message;
    }
}