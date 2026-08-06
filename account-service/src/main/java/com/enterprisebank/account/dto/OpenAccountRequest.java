package com.enterprisebank.account.dto;

import com.enterprisebank.account.entity.AccountType;
import com.enterprisebank.account.entity.CurrencyCode;
import jakarta.validation.constraints.NotNull;

public class OpenAccountRequest {

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotNull(message = "Currency is required")
    private CurrencyCode currency;

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public CurrencyCode getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencyCode currency) {
        this.currency = currency;
    }
}