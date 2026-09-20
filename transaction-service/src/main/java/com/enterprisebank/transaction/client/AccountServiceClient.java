package com.enterprisebank.transaction.client;

import com.enterprisebank.transaction.client.dto.BalanceOperationRequest;
import com.enterprisebank.transaction.client.dto.BalanceOperationResponse;
import com.enterprisebank.transaction.exception.AccountServiceException;
import com.enterprisebank.transaction.exception.AccountServiceUnavailableException;
import com.enterprisebank.transaction.exception.InsufficientFundsException;

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


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

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
         * Retry wraps the actual HTTP request.
         *
         * Network/connectivity failures can be retried
         * according to transaction-service.yml.
         *
         * Business errors such as 422 should not
         * normally be retried.
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
         * Circuit Breaker wraps the retry operation.
         *
         * Spring Cloud Circuit Breaker may execute the
         * supplier on another thread. Because of that,
         * the original downstream exception can be
         * wrapped inside another exception.
         *
         * translateCircuitBreakerFailure() unwraps it.
         */
        return circuitBreaker.run(
                retrySupplier,
                throwable -> {

                    throw translateCircuitBreakerFailure(
                            throwable
                    );
                }
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

            BalanceOperationResponse response =
                    restClient
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

                            .body(
                                    BalanceOperationResponse.class
                            );


            if (response == null) {

                throw new AccountServiceException(
                        "Account Service returned an empty response"
                );
            }


            return response;

        }

        /*
         * IMPORTANT:
         *
         * Do not convert HTTP errors here.
         *
         * We preserve RestClientResponseException so
         * the circuit-breaker failure translator can
         * inspect the real HTTP status.
         *
         * Example:
         *
         * 422 -> insufficient funds
         */
        catch (RestClientResponseException exception) {

            throw exception;
        }


        /*
         * Service discovery/load-balancer failure.
         */
        catch (IllegalStateException exception) {

            throw new AccountServiceUnavailableException(
                    "No Account Service instance is currently available",
                    exception
            );
        }


        /*
         * Network/connection failure.
         */
        catch (ResourceAccessException exception) {

            throw new AccountServiceUnavailableException(
                    "Unable to communicate with Account Service",
                    exception
            );
        }
    }


    // =====================================================
    // CIRCUIT BREAKER FAILURE TRANSLATION
    // =====================================================

    private RuntimeException translateCircuitBreakerFailure(
            Throwable throwable
    ) {

        /*
         * Spring Cloud Circuit Breaker may wrap the
         * original exception.
         *
         * Find the actual underlying exception first.
         */
        Throwable cause =
                unwrapCause(throwable);


        // -------------------------------------------------
        // ALREADY AN INSUFFICIENT FUNDS EXCEPTION
        // -------------------------------------------------

        if (cause
                instanceof InsufficientFundsException exception) {

            return exception;
        }


        // -------------------------------------------------
        // ACCOUNT SERVICE RETURNED HTTP ERROR
        // -------------------------------------------------

        if (cause
                instanceof RestClientResponseException exception) {

            int statusCode =
                    exception
                            .getStatusCode()
                            .value();

            String responseBody =
                    exception
                            .getResponseBodyAsString();


            /*
             * Account Service returns HTTP 422 when
             * there is not enough available balance.
             *
             * Convert that downstream response into
             * our Transaction Service business
             * exception.
             */
            if (statusCode == 422) {

                String message =
                        extractDownstreamMessage(
                                responseBody,
                                "Insufficient available balance"
                        );

                return new InsufficientFundsException(
                        message
                );
            }


            /*
             * Other downstream HTTP errors.
             *
             * Examples:
             *
             * 400 - invalid request
             * 404 - account not found
             * 409 - business conflict
             */
            return new AccountServiceException(
                    "Account Service returned "
                            + exception.getStatusCode()
                            + ": "
                            + responseBody,
                    exception
            );
        }


        // -------------------------------------------------
        // ACCOUNT SERVICE CONNECTIVITY FAILURE
        // -------------------------------------------------

        if (cause
                instanceof AccountServiceUnavailableException exception) {

            return new AccountServiceException(
                    "Account Service is temporarily unavailable",
                    exception
            );
        }


        // -------------------------------------------------
        // EXISTING ACCOUNT SERVICE EXCEPTION
        // -------------------------------------------------

        if (cause
                instanceof AccountServiceException exception) {

            return exception;
        }


        // -------------------------------------------------
        // NETWORK FAILURE
        // -------------------------------------------------

        if (cause
                instanceof ResourceAccessException exception) {

            return new AccountServiceException(
                    "Unable to communicate with Account Service: "
                            + exception.getMessage(),
                    exception
            );
        }


        // -------------------------------------------------
        // UNKNOWN DOWNSTREAM FAILURE
        // -------------------------------------------------

        return new AccountServiceException(
                "Account Service is temporarily unavailable",
                cause
        );
    }


    // =====================================================
    // UNWRAP ORIGINAL EXCEPTION
    // =====================================================

    private Throwable unwrapCause(
            Throwable throwable
    ) {

        Throwable current =
                throwable;


        while (current != null) {

            /*
             * Stop when we reach one of the exception
             * types that contains useful information.
             */

            if (current
                    instanceof RestClientResponseException) {

                return current;
            }


            if (current
                    instanceof InsufficientFundsException) {

                return current;
            }


            if (current
                    instanceof AccountServiceUnavailableException) {

                return current;
            }


            if (current
                    instanceof AccountServiceException) {

                return current;
            }


            if (current
                    instanceof ResourceAccessException) {

                return current;
            }


            /*
             * No deeper exception exists.
             */
            if (current.getCause() == null
                    || current.getCause() == current) {

                break;
            }


            current =
                    current.getCause();
        }


        return current != null
                ? current
                : throwable;
    }


    // =====================================================
    // EXTRACT DOWNSTREAM ERROR MESSAGE
    // =====================================================

    private String extractDownstreamMessage(
            String responseBody,
            String defaultMessage
    ) {

        if (responseBody == null
                || responseBody.isBlank()) {

            return defaultMessage;
        }


        /*
         * Account Service currently returns JSON similar to:
         *
         * {
         *   "error": "Unprocessable Entity",
         *   "message": "Insufficient available balance",
         *   "path": "/internal/accounts/1/debit",
         *   "status": 422
         * }
         *
         * We only need the "message" value.
         */

        String marker =
                "\"message\"";

        int messageIndex =
                responseBody.indexOf(marker);


        if (messageIndex < 0) {

            return defaultMessage;
        }


        int colonIndex =
                responseBody.indexOf(
                        ":",
                        messageIndex
                );


        if (colonIndex < 0) {

            return defaultMessage;
        }


        int firstQuote =
                responseBody.indexOf(
                        "\"",
                        colonIndex + 1
                );


        if (firstQuote < 0) {

            return defaultMessage;
        }


        int secondQuote =
                responseBody.indexOf(
                        "\"",
                        firstQuote + 1
                );


        if (secondQuote < 0) {

            return defaultMessage;
        }


        String message =
                responseBody.substring(
                        firstQuote + 1,
                        secondQuote
                );


        if (message.isBlank()) {

            return defaultMessage;
        }


        return message;
    }
}