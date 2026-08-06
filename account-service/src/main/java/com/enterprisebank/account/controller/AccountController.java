package com.enterprisebank.account.controller;

import com.enterprisebank.account.client.CustomerServiceClient;
import com.enterprisebank.account.client.dto.CustomerProfileResponse;
import com.enterprisebank.account.dto.AccountResponse;
import com.enterprisebank.account.dto.OpenAccountRequest;
import com.enterprisebank.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final CustomerServiceClient customerServiceClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse openAccount(
            @Valid @RequestBody OpenAccountRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader
    ) {

        CustomerProfileResponse customer =
                customerServiceClient.getMyProfile(
                        authorizationHeader
                );

        return accountService.openAccount(
                request,
                customer
        );
    }

    @GetMapping("/me")
    public List<AccountResponse> getMyAccounts(
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader
    ) {

        CustomerProfileResponse customer =
                customerServiceClient.getMyProfile(
                        authorizationHeader
                );

        return accountService.getMyAccounts(
                customer.getId()
        );
    }

    @GetMapping("/{id}")
    public AccountResponse getAccountById(
            @PathVariable("id") Long id
    ) {
        return accountService.getAccountById(id);
    }

    @GetMapping("/number/{accountNumber}")
    public AccountResponse getAccountByNumber(
            @PathVariable("accountNumber") String accountNumber
    ) {
        return accountService.getAccountByNumber(accountNumber);
    }

    @GetMapping("/customer/{customerId}")
    public List<AccountResponse> getAccountsByCustomerId(
            @PathVariable("customerId") Long customerId
    ) {
        return accountService.getAccountsByCustomerId(customerId);
    }
}