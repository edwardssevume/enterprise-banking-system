package com.enterprisebank.transaction.client;

import com.enterprisebank.transaction.client.dto.BalanceOperationResponse;
import com.enterprisebank.transaction.client.dto.BalanceOperationRequest;
import com.enterprisebank.transaction.exception.AccountServiceException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class AccountServiceClient {

    private final RestClient restClient;

    public AccountServiceClient(
            @Qualifier("accountServiceRestClientBuilder")
            RestClient.Builder builder
    ) {
        this.restClient = builder
                .baseUrl("http://account-service")
                .build();
    }

    public BalanceOperationResponse credit(
            Long accountId,
            BalanceOperationRequest request,
            String authorizationHeader
    ) {
        return executeBalanceOperation(
                accountId,
                "credit",
                request,
                authorizationHeader
        );
    }

    public BalanceOperationResponse debit(
            Long accountId,
            BalanceOperationRequest request,
            String authorizationHeader
    ) {
        return executeBalanceOperation(
                accountId,
                "debit",
                request,
                authorizationHeader
        );
    }

    private BalanceOperationResponse executeBalanceOperation(
            Long accountId,
            String operation,
            BalanceOperationRequest request,
            String authorizationHeader
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
                            HttpHeaders.AUTHORIZATION,
                            authorizationHeader
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

            throw new AccountServiceException(
                    "Account Service returned "
                            + exception.getStatusCode()
                            + ": "
                            + exception.getResponseBodyAsString(),
                    exception
            );

        } catch (RestClientException exception) {

            throw new AccountServiceException(
                    "Unable to communicate with Account Service: "
                            + exception.getMessage(),
                    exception
            );
        }
    }
}