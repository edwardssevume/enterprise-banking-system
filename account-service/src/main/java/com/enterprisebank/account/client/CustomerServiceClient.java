package com.enterprisebank.account.client;

import com.enterprisebank.account.client.dto.CustomerProfileResponse;
import com.enterprisebank.account.exception.CustomerServiceException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class CustomerServiceClient {

    private final RestClient restClient;

    public CustomerServiceClient(
            @Qualifier("customerServiceRestClientBuilder")
            RestClient.Builder restClientBuilder
    ) {
        this.restClient = restClientBuilder
                .baseUrl("http://customer-service")
                .build();
    }

    public CustomerProfileResponse getMyProfile(
            String authorizationHeader
    ) {
        try {
            CustomerProfileResponse response = restClient
                    .get()
                    .uri("/api/customers/me")
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            authorizationHeader
                    )
                    .retrieve()
                    .body(CustomerProfileResponse.class);

            if (response == null) {
                throw new CustomerServiceException(
                        "Customer Service returned an empty response"
                );
            }

            return response;

        } catch (RestClientException exception) {
            throw new CustomerServiceException(
                    "Unable to verify customer profile",
                    exception
            );
        }
    }
}