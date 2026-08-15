package com.enterprisebank.account.client;

import com.enterprisebank.account.client.dto.CustomerProfileResponse;
import com.enterprisebank.account.exception.CustomerServiceException;
import com.enterprisebank.account.exception.CustomerServiceUnavailableException;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.function.Supplier;

@Component
public class CustomerServiceClient {

    private static final String CIRCUIT_BREAKER_NAME =
            "customerService";

    private static final String RETRY_NAME =
            "customerService";

    private final RestClient restClient;

    private final CircuitBreakerFactory<?, ?>
            circuitBreakerFactory;

    private final RetryRegistry retryRegistry;

    public CustomerServiceClient(
            @Qualifier("customerServiceRestClientBuilder")
            RestClient.Builder restClientBuilder,

            CircuitBreakerFactory<?, ?> circuitBreakerFactory,

            RetryRegistry retryRegistry
    ) {

        this.restClient = restClientBuilder
                .baseUrl("http://customer-service")
                .build();

        this.circuitBreakerFactory =
                circuitBreakerFactory;

        this.retryRegistry =
                retryRegistry;
    }

    public CustomerProfileResponse getMyProfile(
            String authorizationHeader
    ) {

        CircuitBreaker circuitBreaker =
                circuitBreakerFactory.create(
                        CIRCUIT_BREAKER_NAME
                );

        Retry retry =
                retryRegistry.retry(
                        RETRY_NAME
                );

        Supplier<CustomerProfileResponse> supplier =
                Retry.decorateSupplier(
                        retry,
                        () -> callCustomerService(
                                authorizationHeader
                        )
                );

        return circuitBreaker.run(
                supplier,
                this::handleCircuitBreakerFailure
        );
    }

    private CustomerProfileResponse callCustomerService(
            String authorizationHeader
    ) {

        try {

            CustomerProfileResponse response =
                    restClient
                            .get()
                            .uri("/api/customers/me")
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    authorizationHeader
                            )
                            .retrieve()
                            .body(
                                    CustomerProfileResponse.class
                            );

            if (response == null) {
                throw new CustomerServiceException(
                        "Customer Service returned an empty response"
                );
            }

            return response;

        } catch (RestClientResponseException exception) {

            throw exception;

        } catch (IllegalStateException exception) {

            throw new CustomerServiceUnavailableException(
                    "No Customer Service instance is currently available",
                    exception
            );

        } catch (ResourceAccessException exception) {

            throw new CustomerServiceUnavailableException(
                    "Unable to communicate with Customer Service",
                    exception
            );

        } catch (RestClientException exception) {

            throw new CustomerServiceException(
                    "Unable to verify customer profile",
                    exception
            );
        }
    }

    private CustomerProfileResponse handleCircuitBreakerFailure(
            Throwable throwable
    ) {

        if (throwable
                instanceof CustomerServiceUnavailableException exception) {

            throw new CustomerServiceException(
                    "Customer Service is temporarily unavailable",
                    exception
            );
        }

        if (throwable
                instanceof RestClientResponseException exception) {

            throw new CustomerServiceException(
                    "Customer Service returned "
                            + exception.getStatusCode()
                            + ": "
                            + exception.getResponseBodyAsString(),
                    exception
            );
        }

        if (throwable
                instanceof CustomerServiceException exception) {

            throw exception;
        }

        throw new CustomerServiceException(
                "Customer Service is temporarily unavailable",
                throwable
        );
    }
}