package com.enterprisebank.account.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI accountServiceApi() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title(
                                        "Enterprise Banking Account Service API"
                                )
                                .description(
                                        "Bank account management service "
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