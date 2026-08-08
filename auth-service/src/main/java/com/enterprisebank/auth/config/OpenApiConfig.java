package com.enterprisebank.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI authServiceApi() {

        return new OpenAPI()

                .info(

                        new Info()

                                .title(
                                        "Enterprise Banking Auth Service API"
                                )

                                .description(
                                        "Authentication and authorization service"
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