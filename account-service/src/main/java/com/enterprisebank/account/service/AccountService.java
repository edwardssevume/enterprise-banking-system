package com.enterprisebank.account.service;

import com.enterprisebank.account.client.dto.CustomerProfileResponse;
import com.enterprisebank.account.dto.AccountResponse;
import com.enterprisebank.account.dto.OpenAccountRequest;
import com.enterprisebank.account.entity.Account;
import com.enterprisebank.account.entity.AccountStatus;
import com.enterprisebank.account.exception.AccountNotFoundException;
import com.enterprisebank.account.exception.DuplicateAccountException;
import com.enterprisebank.account.repository.AccountRepository;
import com.enterprisebank.account.dto.BalanceOperationRequest;
import com.enterprisebank.account.dto.BalanceOperationResponse;
import com.enterprisebank.account.exception.InsufficientFundsException;
import com.enterprisebank.account.exception.InvalidAccountOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final AccountRepository accountRepository;

    @Transactional
    public AccountResponse openAccount(
            OpenAccountRequest request,
            CustomerProfileResponse customer
    ) {
        validateCustomer(customer);

        Long customerId = customer.getId();

        boolean duplicateExists =
                accountRepository
                        .existsByCustomerIdAndAccountTypeAndCurrencyAndStatusNot(
                                customerId,
                                request.getAccountType(),
                                request.getCurrency(),
                                AccountStatus.CLOSED
                        );

        if (duplicateExists) {
            throw new DuplicateAccountException(
                    "Customer already has an active account "
                            + "with this type and currency"
            );
        }

        Account account = Account.builder()
                .accountNumber(generateUniqueAccountNumber())
                .customerId(customerId)
                .accountType(request.getAccountType())
                .currency(request.getCurrency())
                .ledgerBalance(BigDecimal.ZERO)
                .availableBalance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();

        Account savedAccount = accountRepository.save(account);

        return mapToResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found with ID: " + id
                ));

        return mapToResponse(account);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(
            String accountNumber
    ) {
        Account account = accountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found with number: "
                                + accountNumber
                ));

        return mapToResponse(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByCustomerId(
            Long customerId
    ) {
        return findAccountsByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts(
            Long customerId
    ) {
        return findAccountsByCustomerId(customerId);
    }

    private List<AccountResponse> findAccountsByCustomerId(
            Long customerId
    ) {
        return accountRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BalanceOperationResponse creditAccount(
            Long accountId,
            BalanceOperationRequest request
    ) {
        Account account = getActiveAccount(accountId);

        validateCurrency(account, request.getCurrency());

        BigDecimal newLedgerBalance = account
                .getLedgerBalance()
                .add(request.getAmount());

        BigDecimal newAvailableBalance = account
                .getAvailableBalance()
                .add(request.getAmount());

        account.setLedgerBalance(newLedgerBalance);
        account.setAvailableBalance(newAvailableBalance);

        Account savedAccount = accountRepository.save(account);

        return mapToBalanceResponse(
                savedAccount,
                request.getTransactionReference(),
                "Account credited successfully"
        );
    }

    @Transactional
    public BalanceOperationResponse debitAccount(
            Long accountId,
            BalanceOperationRequest request
    ) {
        Account account = getActiveAccount(accountId);

        validateCurrency(account, request.getCurrency());

        if (account.getAvailableBalance()
                .compareTo(request.getAmount()) < 0) {

            throw new InsufficientFundsException(
                    "Insufficient available balance"
            );
        }

        BigDecimal newLedgerBalance = account
                .getLedgerBalance()
                .subtract(request.getAmount());

        BigDecimal newAvailableBalance = account
                .getAvailableBalance()
                .subtract(request.getAmount());

        account.setLedgerBalance(newLedgerBalance);
        account.setAvailableBalance(newAvailableBalance);

        Account savedAccount = accountRepository.save(account);

        return mapToBalanceResponse(
                savedAccount,
                request.getTransactionReference(),
                "Account debited successfully"
        );
    }

    private void validateCustomer(
            CustomerProfileResponse customer
    ) {
        if (customer == null || customer.getId() == null) {
            throw new IllegalStateException(
                    "Customer profile is missing or invalid"
            );
        }

        if (!"ACTIVE".equalsIgnoreCase(customer.getStatus())) {
            throw new IllegalStateException(
                    "Only active customers can open accounts"
            );
        }
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;

        do {
            accountNumber = generateAccountNumber();
        } while (accountRepository.existsByAccountNumber(
                accountNumber
        ));

        return accountNumber;
    }

    private String generateAccountNumber() {
        StringBuilder number = new StringBuilder("10");

        for (int i = 0; i < 10; i++) {
            number.append(RANDOM.nextInt(10));
        }

        return number.toString();
    }

    private AccountResponse mapToResponse(
            Account account
    ) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getCustomerId(),
                account.getAccountType(),
                account.getCurrency(),
                account.getLedgerBalance(),
                account.getAvailableBalance(),
                account.getStatus(),
                account.getVersion(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    private Account getActiveAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found with ID: " + accountId
                ));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidAccountOperationException(
                    "Account is not active"
            );
        }

        return account;
    }

    private void validateCurrency(
            Account account,
            String requestedCurrency
    ) {
        if (!account.getCurrency()
                .name()
                .equalsIgnoreCase(requestedCurrency)) {

            throw new InvalidAccountOperationException(
                    "Transaction currency does not match "
                            + "the account currency"
            );
        }
    }

    private BalanceOperationResponse mapToBalanceResponse(
            Account account,
            String transactionReference,
            String message
    ) {
        return new BalanceOperationResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getLedgerBalance(),
                account.getAvailableBalance(),
                account.getCurrency().name(),
                transactionReference,
                message
        );
    }
}