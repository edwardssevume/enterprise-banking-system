package com.enterprisebank.transaction.event;

import com.enterprisebank.transaction.entity.BankTransaction;

public record TransactionCommittedEvent(
        BankTransaction transaction
) {
}