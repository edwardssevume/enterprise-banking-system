package com.enterprisebank.account.config;

import com.enterprisebank.account.security.InternalServiceAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final InternalServiceAuthenticationFilter
            internalServiceAuthenticationFilter;

    public SecurityConfig(
            InternalServiceAuthenticationFilter
                    internalServiceAuthenticationFilter
    ) {
        this.internalServiceAuthenticationFilter =
                internalServiceAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            Converter<Jwt, AbstractAuthenticationToken>
                    jwtAuthenticationConverter
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        .dispatcherTypeMatchers(
                                DispatcherType.ERROR
                        ).permitAll()

                        .requestMatchers(
                                "/error",
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()

                        // Internal service-to-service endpoints
                        .requestMatchers(
                                "/internal/accounts/**"
                        ).hasRole("INTERNAL_SERVICE")

                        // Customer or admin can open an account
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/accounts"
                        ).hasAnyRole(
                                "CUSTOMER",
                                "ADMIN"
                        )

                        // Authenticated user can view own accounts
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/accounts/me"
                        ).authenticated()

                        // Employee or admin can access general account APIs
                        .requestMatchers(
                                "/api/accounts/**"
                        ).hasAnyRole(
                                "EMPLOYEE",
                                "ADMIN"
                        )

                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(resourceServer ->
                        resourceServer.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                )

                .addFilterBefore(
                        internalServiceAuthenticationFilter,
                        BearerTokenAuthenticationFilter.class
                );

        return http.build();
    }
}