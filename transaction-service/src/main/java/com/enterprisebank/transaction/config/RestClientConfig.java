package com.enterprisebank.transaction.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    @Primary
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean(name = "accountServiceRestClientBuilder")
    @LoadBalanced
    public RestClient.Builder
    accountServiceRestClientBuilder() {
        return RestClient.builder();
    }
}