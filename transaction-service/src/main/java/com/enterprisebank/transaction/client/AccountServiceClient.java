package com.enterprisebank.transaction.client;

import com.enterprisebank.transaction.client.dto.BalanceOperationRequest;
import com.enterprisebank.transaction.client.dto.BalanceOperationResponse;
import com.enterprisebank.transaction.exception.AccountServiceException;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import com.enterprisebank.transaction.exception.AccountServiceUnavailableException;

import java.util.function.Supplier;

@Component
public class AccountServiceClient {

    private static final String INTERNAL_SERVICE_HEADER =
            "X-Internal-Service-Key";

    private static final String CIRCUIT_BREAKER_NAME =
            "accountService";

    private final RestClient restClient;
    private final String internalServiceSecret;

    private final CircuitBreakerFactory<?, ?>
            circuitBreakerFactory;

    private final RetryRegistry retryRegistry;


    public AccountServiceClient(
            @Qualifier("accountServiceRestClientBuilder")
            RestClient.Builder builder,

            @Value("${internal.service.secret}")
            String internalServiceSecret,

            CircuitBreakerFactory<?, ?>
                    circuitBreakerFactory,

            RetryRegistry retryRegistry
    ) {

        this.restClient = builder
                .baseUrl("http://account-service")
                .build();

        this.internalServiceSecret =
                internalServiceSecret;

        this.circuitBreakerFactory =
                circuitBreakerFactory;

        this.retryRegistry =
                retryRegistry;
    }


    // =====================================================
    // CREDIT
    // =====================================================

    public BalanceOperationResponse credit(
            Long accountId,
            BalanceOperationRequest request
    ) {

        return executeBalanceOperation(
                accountId,
                "credit",
                request
        );
    }


    // =====================================================
    // DEBIT
    // =====================================================

    public BalanceOperationResponse debit(
            Long accountId,
            BalanceOperationRequest request
    ) {

        return executeBalanceOperation(
                accountId,
                "debit",
                request
        );
    }


    // =====================================================
    // CIRCUIT BREAKER + RETRY
    // =====================================================

    private BalanceOperationResponse executeBalanceOperation(
            Long accountId,
            String operation,
            BalanceOperationRequest request
    ) {

        CircuitBreaker circuitBreaker =
                circuitBreakerFactory.create(
                        CIRCUIT_BREAKER_NAME
                );

        Retry retry =
                retryRegistry.retry(
                        CIRCUIT_BREAKER_NAME
                );

        /*
         * Retry wraps the actual HTTP call.
         *
         * Only exceptions configured as retryable
         * in transaction-service.yml will be retried.
         */
        Supplier<BalanceOperationResponse> retrySupplier =
                Retry.decorateSupplier(
                        retry,
                        () -> callAccountService(
                                accountId,
                                operation,
                                request
                        )
                );

        /*
         * Circuit Breaker wraps the whole retry operation.
         *
         * This means the Circuit Breaker sees the final
         * result after Retry has finished trying.
         */
        return circuitBreaker.run(
                retrySupplier,
                this::handleCircuitBreakerFailure
        );
    }


    // =====================================================
    // ACTUAL ACCOUNT SERVICE HTTP CALL
    // =====================================================

    private BalanceOperationResponse callAccountService(
            Long accountId,
            String operation,
            BalanceOperationRequest request
    ) {

        try {

            BalanceOperationResponse response = restClient
                    .post()
                    .uri(
                            "/internal/accounts/{accountId}/{operation}",
                            accountId,
                            operation
                    )
                    .header(
                            INTERNAL_SERVICE_HEADER,
                            internalServiceSecret
                    )
                    .body(request)
                    .retrieve()
                    .body(BalanceOperationResponse.class);

            if (response == null) {
                throw new AccountServiceException(
                        "Account Service returned an empty response"
                );
            }

            return response;

        } catch (RestClientResponseException exception) {

            throw exception;

        } catch (IllegalStateException exception) {

            throw new AccountServiceUnavailableException(
                    "No Account Service instance is currently available",
                    exception
            );

        } catch (ResourceAccessException exception) {

            throw new AccountServiceUnavailableException(
                    "Unable to communicate with Account Service",
                    exception
            );
        }
    }


    // =====================================================
    // FAILURE HANDLING
    // =====================================================

    private BalanceOperationResponse handleCircuitBreakerFailure(
            Throwable throwable
    ) {

        /*
         * HTTP 4xx / 5xx response returned by Account Service.
         *
         * Examples:
         * 400 - Currency mismatch
         * 404 - Account not found
         * 409 - Business conflict
         *
         * These should NOT normally be retried.
         */
        if (throwable
                instanceof RestClientResponseException exception) {

            throw new AccountServiceException(
                    "Account Service returned "
                            + exception.getStatusCode()
                            + ": "
                            + exception.getResponseBodyAsString(),
                    exception
            );
        }


        /*
         * Network / connection problem.
         *
         * This is the type of problem that Retry
         * is allowed to retry.
         */
        if (throwable
                instanceof ResourceAccessException exception) {

            throw new AccountServiceException(
                    "Unable to communicate with Account Service: "
                            + exception.getMessage(),
                    exception
            );
        }


        /*
         * Preserve our own business/client exception.
         */
        if (throwable
                instanceof AccountServiceException accountException) {

            throw accountException;
        }

        if (throwable
                instanceof AccountServiceUnavailableException exception) {

            throw new AccountServiceException(
                    "Account Service is temporarily unavailable",
                    exception
            );
        }


        /*
         * Circuit OPEN or some unexpected downstream problem.
         */
        throw new AccountServiceException(
                "Account Service is temporarily unavailable",
                throwable
        );
    }
}