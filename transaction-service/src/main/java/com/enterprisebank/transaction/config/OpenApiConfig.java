package com.enterprisebank.transaction.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI transactionServiceApi() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title(
                                        "Enterprise Banking Transaction Service API"
                                )
                                .description(
                                        "Deposit, withdrawal, transfer, and "
                                                + "transaction history service "
                                                + "for the Enterprise Banking System"
                                )
                                .version("1.0.0")
                                .contact(
                                        new Contact()
                                                .name("Edward Ssevume")
                                                .email("developer@example.com")
                                )
                                .license(
                                        new License()
                                                .name("MIT")
                                )
                );
    }
}