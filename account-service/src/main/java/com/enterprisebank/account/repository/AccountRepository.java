package com.enterprisebank.account.repository;

import com.enterprisebank.account.entity.Account;
import com.enterprisebank.account.entity.AccountStatus;
import com.enterprisebank.account.entity.AccountType;
import com.enterprisebank.account.entity.CurrencyCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository
        extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(
            String accountNumber
    );

    List<Account> findByCustomerId(
            Long customerId
    );

    List<Account> findByCustomerIdAndStatus(
            Long customerId,
            AccountStatus status
    );

    boolean existsByAccountNumber(
            String accountNumber
    );

    boolean existsByCustomerIdAndAccountTypeAndCurrencyAndStatusNot(
            Long customerId,
            AccountType accountType,
            CurrencyCode currency,
            AccountStatus status
    );
}