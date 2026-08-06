package com.enterprisebank.transaction.controller;

import com.enterprisebank.transaction.dto.DepositRequest;
import com.enterprisebank.transaction.dto.TransactionResponse;
import com.enterprisebank.transaction.dto.TransferRequest;
import com.enterprisebank.transaction.dto.WithdrawalRequest;
import com.enterprisebank.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse deposit(
            @Valid @RequestBody DepositRequest request,
            @RequestHeader("Idempotency-Key")
            String idempotencyKey,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return transactionService.deposit(
                request,
                idempotencyKey,
                extractUserId(jwt),
                authorizationHeader
        );
    }

    @PostMapping("/withdrawal")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse withdrawal(
            @Valid @RequestBody WithdrawalRequest request,
            @RequestHeader("Idempotency-Key")
            String idempotencyKey,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return transactionService.withdrawal(
                request,
                idempotencyKey,
                extractUserId(jwt),
                authorizationHeader
        );
    }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse transfer(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader("Idempotency-Key")
            String idempotencyKey,
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return transactionService.transfer(
                request,
                idempotencyKey,
                extractUserId(jwt),
                authorizationHeader
        );
    }

    @GetMapping("/me")
    public List<TransactionResponse> getMyTransactions(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return transactionService.getMyTransactions(
                extractUserId(jwt)
        );
    }

    @GetMapping("/reference/{reference}")
    public TransactionResponse getByReference(
            @PathVariable String reference
    ) {
        return transactionService.getByReference(reference);
    }

    private Long extractUserId(Jwt jwt) {
        Number userId = jwt.getClaim("userId");

        if (userId == null) {
            throw new IllegalStateException(
                    "JWT does not contain userId"
            );
        }

        return userId.longValue();
    }
}