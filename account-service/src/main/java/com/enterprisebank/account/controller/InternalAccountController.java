package com.enterprisebank.account.controller;

import com.enterprisebank.account.dto.BalanceOperationRequest;
import com.enterprisebank.account.dto.BalanceOperationResponse;
import com.enterprisebank.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/accounts")
@RequiredArgsConstructor
public class InternalAccountController {

    private final AccountService accountService;

    @PostMapping("/{accountId}/credit")
    public BalanceOperationResponse creditAccount(
            @PathVariable("accountId") Long accountId,
            @Valid @RequestBody BalanceOperationRequest request
    ) {
        return accountService.creditAccount(
                accountId,
                request
        );
    }

    @PostMapping("/{accountId}/debit")
    public BalanceOperationResponse debitAccount(
            @PathVariable("accountId") Long accountId,
            @Valid @RequestBody BalanceOperationRequest request
    ) {
        return accountService.debitAccount(
                accountId,
                request
        );
    }
}