package com.enterprisebank.account.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    /*
     * Default HTTP builder.
     *
     * Eureka will use this builder for:
     * http://localhost:8761/eureka/
     */
    @Bean
    @Primary
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    /*
     * Load-balanced builder used only for calls made through
     * Eureka service names, such as:
     * http://customer-service
     */
    @Bean
    @LoadBalanced
    @Qualifier("customerServiceRestClientBuilder")
    public RestClient.Builder customerServiceRestClientBuilder() {
        return RestClient.builder();
    }
}