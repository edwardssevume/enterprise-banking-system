package com.enterprisebank.transaction.client.dto;

import java.math.BigDecimal;

public class BalanceOperationRequest {

    private BigDecimal amount;
    private String currency;
    private String transactionReference;

    public BalanceOperationRequest(
            BigDecimal amount,
            String currency,
            String transactionReference
    ) {
        this.amount = amount;
        this.currency = currency;
        this.transactionReference = transactionReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getTransactionReference() {
        return transactionReference;
    }
}