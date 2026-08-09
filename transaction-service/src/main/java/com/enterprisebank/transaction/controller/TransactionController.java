package com.enterprisebank.transaction.controller;

import com.enterprisebank.transaction.dto.DepositRequest;
import com.enterprisebank.transaction.dto.TransactionResponse;
import com.enterprisebank.transaction.dto.TransferRequest;
import com.enterprisebank.transaction.dto.WithdrawalRequest;
import com.enterprisebank.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Transactions",
        description = "Deposit, withdrawal, transfer, and transaction history operations"
)
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(
            summary = "Deposit money",
            description = "Credits funds to a bank account"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Deposit completed successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or currency mismatch"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content
            )
    })
    @PostMapping("/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse deposit(
            @Valid @RequestBody DepositRequest request,

            @Parameter(
                    description = "Unique key used to prevent duplicate transaction processing",
                    required = true,
                    example = "deposit-test-001"
            )
            @RequestHeader("Idempotency-Key")
            String idempotencyKey,

            @AuthenticationPrincipal Jwt jwt
    ) {
        return transactionService.deposit(
                request,
                idempotencyKey,
                extractUserId(jwt)
        );
    }

    @Operation(
            summary = "Withdraw money",
            description = "Debits funds from a bank account"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Withdrawal completed successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid transaction request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Insufficient funds"
            )
    })
    @PostMapping("/withdrawal")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse withdrawal(
            @Valid @RequestBody WithdrawalRequest request,

            @Parameter(
                    description = "Unique key used to prevent duplicate transaction processing",
                    required = true,
                    example = "withdrawal-test-001"
            )
            @RequestHeader("Idempotency-Key")
            String idempotencyKey,

            @AuthenticationPrincipal Jwt jwt
    ) {
        return transactionService.withdrawal(
                request,
                idempotencyKey,
                extractUserId(jwt)
        );
    }

    @Operation(
            summary = "Transfer money",
            description = "Transfers funds from one account to another"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Transfer completed successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid transfer request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Insufficient funds"
            )
    })
    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse transfer(
            @Valid @RequestBody TransferRequest request,

            @Parameter(
                    description = "Unique key used to prevent duplicate transaction processing",
                    required = true,
                    example = "transfer-test-001"
            )
            @RequestHeader("Idempotency-Key")
            String idempotencyKey,

            @AuthenticationPrincipal Jwt jwt
    ) {
        return transactionService.transfer(
                request,
                idempotencyKey,
                extractUserId(jwt)
        );
    }

    @Operation(
            summary = "Get my transactions",
            description = "Returns transaction history for the authenticated user"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Transaction history retrieved successfully"
    )
    @GetMapping("/me")
    public List<TransactionResponse> getMyTransactions(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return transactionService.getMyTransactions(
                extractUserId(jwt)
        );
    }

    @Operation(
            summary = "Get transaction by reference",
            description = "Retrieves a transaction using its unique transaction reference"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transaction found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found"
            )
    })
    @GetMapping("/reference/{reference}")
    public TransactionResponse getByReference(
            @Parameter(
                    description = "Unique transaction reference",
                    example = "TXN-47BA21A3404C4740A5D0"
            )
            @PathVariable("reference")
            String reference
    ) {
        return transactionService.getByReference(
                reference
        );
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