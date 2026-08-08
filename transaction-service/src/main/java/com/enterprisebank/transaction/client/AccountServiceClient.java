package com.enterprisebank.transaction.client;

import com.enterprisebank.transaction.client.dto.BalanceOperationRequest;
import com.enterprisebank.transaction.client.dto.BalanceOperationResponse;
import com.enterprisebank.transaction.exception.AccountServiceException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class AccountServiceClient {

    private static final String INTERNAL_SERVICE_HEADER =
            "X-Internal-Service-Key";

    private final RestClient restClient;
    private final String internalServiceSecret;

    public AccountServiceClient(
            @Qualifier("accountServiceRestClientBuilder")
            RestClient.Builder builder,

            @Value("${internal.service.secret}")
            String internalServiceSecret
    ) {
        this.restClient = builder
                .baseUrl("http://account-service")
                .build();

        this.internalServiceSecret =
                internalServiceSecret;
    }

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

    private BalanceOperationResponse executeBalanceOperation(
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