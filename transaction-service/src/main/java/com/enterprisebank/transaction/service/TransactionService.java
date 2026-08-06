package com.enterprisebank.transaction.service;

import com.enterprisebank.transaction.client.AccountServiceClient;
import com.enterprisebank.transaction.client.dto.BalanceOperationRequest;
import com.enterprisebank.transaction.dto.DepositRequest;
import com.enterprisebank.transaction.dto.TransactionResponse;
import com.enterprisebank.transaction.dto.TransferRequest;
import com.enterprisebank.transaction.dto.WithdrawalRequest;
import com.enterprisebank.transaction.entity.BankTransaction;
import com.enterprisebank.transaction.entity.TransactionDirection;
import com.enterprisebank.transaction.entity.TransactionStatus;
import com.enterprisebank.transaction.entity.TransactionType;
import com.enterprisebank.transaction.exception.InvalidTransactionException;
import com.enterprisebank.transaction.exception.TransactionNotFoundException;
import com.enterprisebank.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;

    public TransactionResponse deposit(
            DepositRequest request,
            String idempotencyKey,
            Long userId,
            String authorizationHeader
    ) {
        TransactionResponse existing =
                findExistingCompletedTransaction(idempotencyKey);

        if (existing != null) {
            return existing;
        }
        BankTransaction transaction = beginTransaction(
                idempotencyKey,
                TransactionType.DEPOSIT,
                TransactionDirection.CREDIT,
                null,
                request.getAccountId(),
                request.getAmount(),
                request.getCurrency(),
                request.getDescription(),
                userId
        );

        try {
            accountServiceClient.credit(
                    request.getAccountId(),
                    createBalanceRequest(transaction),
                    authorizationHeader
            );

            return completeTransaction(transaction);

        } catch (RuntimeException exception) {
            failTransaction(transaction, exception.getMessage());
            throw exception;
        }
    }

    public TransactionResponse withdrawal(
            WithdrawalRequest request,
            String idempotencyKey,
            Long userId,
            String authorizationHeader
    ) {
        TransactionResponse existing =
                findExistingCompletedTransaction(idempotencyKey);

        if (existing != null) {
            return existing;
        }
        BankTransaction transaction = beginTransaction(
                idempotencyKey,
                TransactionType.WITHDRAWAL,
                TransactionDirection.DEBIT,
                request.getAccountId(),
                null,
                request.getAmount(),
                request.getCurrency(),
                request.getDescription(),
                userId
        );

        try {
            accountServiceClient.debit(
                    request.getAccountId(),
                    createBalanceRequest(transaction),
                    authorizationHeader
            );

            return completeTransaction(transaction);

        } catch (RuntimeException exception) {
            failTransaction(transaction, exception.getMessage());
            throw exception;
        }
    }

    public TransactionResponse transfer(
            TransferRequest request,
            String idempotencyKey,
            Long userId,
            String authorizationHeader
    ) {
        TransactionResponse existing =
                findExistingCompletedTransaction(idempotencyKey);

        if (existing != null) {
            return existing;
        }
        if (request.getSourceAccountId().equals(
                request.getDestinationAccountId()
        )) {
            throw new InvalidTransactionException(
                    "Source and destination accounts "
                            + "must be different"
            );
        }

        BankTransaction transaction = beginTransaction(
                idempotencyKey,
                TransactionType.TRANSFER,
                TransactionDirection.DEBIT,
                request.getSourceAccountId(),
                request.getDestinationAccountId(),
                request.getAmount(),
                request.getCurrency(),
                request.getDescription(),
                userId
        );

        try {
            BalanceOperationRequest balanceRequest =
                    createBalanceRequest(transaction);

            accountServiceClient.debit(
                    request.getSourceAccountId(),
                    balanceRequest,
                    authorizationHeader
            );

            try {
                accountServiceClient.credit(
                        request.getDestinationAccountId(),
                        balanceRequest,
                        authorizationHeader
                );

            } catch (RuntimeException creditException) {
                /*
                 * Compensation:
                 * Return the amount to the source account when
                 * destination crediting fails.
                 */
                accountServiceClient.credit(
                        request.getSourceAccountId(),
                        balanceRequest,
                        authorizationHeader
                );

                throw creditException;
            }

            return completeTransaction(transaction);

        } catch (RuntimeException exception) {
            failTransaction(transaction, exception.getMessage());
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public TransactionResponse getByReference(
            String transactionReference
    ) {
        BankTransaction transaction = transactionRepository
                .findByTransactionReference(
                        transactionReference
                )
                .orElseThrow(() ->
                        new TransactionNotFoundException(
                                "Transaction not found: "
                                        + transactionReference
                        )
                );

        return mapToResponse(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getMyTransactions(
            Long userId
    ) {
        return transactionRepository
                .findByInitiatedByUserIdOrderByCreatedAtDesc(
                        userId
                )
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    protected BankTransaction beginTransaction(
            String idempotencyKey,
            TransactionType type,
            TransactionDirection direction,
            Long sourceAccountId,
            Long destinationAccountId,
            java.math.BigDecimal amount,
            String currency,
            String description,
            Long userId
    ) {
        return transactionRepository
                .findByIdempotencyKey(idempotencyKey)
                .orElseGet(() -> {
                    BankTransaction transaction =
                            BankTransaction.builder()
                                    .transactionReference(
                                            generateReference()
                                    )
                                    .idempotencyKey(idempotencyKey)
                                    .transactionType(type)
                                    .status(
                                            TransactionStatus.PROCESSING
                                    )
                                    .direction(direction)
                                    .sourceAccountId(
                                            sourceAccountId
                                    )
                                    .destinationAccountId(
                                            destinationAccountId
                                    )
                                    .amount(amount)
                                    .currency(
                                            currency.trim()
                                                    .toUpperCase(
                                                            Locale.ROOT
                                                    )
                                    )
                                    .description(
                                            normalizeDescription(
                                                    description
                                            )
                                    )
                                    .initiatedByUserId(userId)
                                    .build();

                    return transactionRepository.save(
                            transaction
                    );
                });
    }

    @Transactional
    protected TransactionResponse completeTransaction(
            BankTransaction transaction
    ) {
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setFailureReason(null);
        transaction.setCompletedAt(LocalDateTime.now());

        return mapToResponse(
                transactionRepository.save(transaction)
        );
    }

    @Transactional
    protected void failTransaction(
            BankTransaction transaction,
            String reason
    ) {
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setFailureReason(
                limitFailureReason(reason)
        );

        transactionRepository.save(transaction);
    }

    private BalanceOperationRequest createBalanceRequest(
            BankTransaction transaction
    ) {
        return new BalanceOperationRequest(
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getTransactionReference()
        );
    }

    private String generateReference() {
        return "TXN-"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 20)
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }

    private String limitFailureReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return "Transaction failed";
        }

        return reason.length() <= 500
                ? reason
                : reason.substring(0, 500);
    }

    private TransactionResponse findExistingCompletedTransaction(
            String idempotencyKey
    ) {
        return transactionRepository
                .findByIdempotencyKey(idempotencyKey)
                .map(this::mapToResponse)
                .orElse(null);
    }

    private TransactionResponse mapToResponse(
            BankTransaction transaction
    ) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionReference(),
                transaction.getIdempotencyKey(),
                transaction.getTransactionType(),
                transaction.getStatus(),
                transaction.getDirection(),
                transaction.getSourceAccountId(),
                transaction.getDestinationAccountId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getDescription(),
                transaction.getInitiatedByUserId(),
                transaction.getFailureReason(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt(),
                transaction.getCompletedAt()
        );
    }
}