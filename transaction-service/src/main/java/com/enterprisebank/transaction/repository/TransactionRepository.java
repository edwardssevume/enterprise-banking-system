package com.enterprisebank.transaction.repository;

import com.enterprisebank.transaction.entity.BankTransaction;
import com.enterprisebank.transaction.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository
        extends JpaRepository<BankTransaction, Long> {

    Optional<BankTransaction> findByTransactionReference(
            String transactionReference
    );

    Optional<BankTransaction> findByIdempotencyKey(
            String idempotencyKey
    );

    List<BankTransaction> findBySourceAccountIdOrderByCreatedAtDesc(
            Long sourceAccountId
    );

    List<BankTransaction>
    findByDestinationAccountIdOrderByCreatedAtDesc(
            Long destinationAccountId
    );

    List<BankTransaction> findByInitiatedByUserIdOrderByCreatedAtDesc(
            Long initiatedByUserId
    );

    List<BankTransaction> findByStatusOrderByCreatedAtDesc(
            TransactionStatus status
    );

    boolean existsByTransactionReference(
            String transactionReference
    );

    boolean existsByIdempotencyKey(
            String idempotencyKey
    );
}