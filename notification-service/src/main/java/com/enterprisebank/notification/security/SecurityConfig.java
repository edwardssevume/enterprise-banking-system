package com.enterprisebank.notification.security;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final Converter<Jwt, AbstractAuthenticationToken>
            jwtAuthenticationConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
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
                        )
                        .permitAll()

                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        )
                        .permitAll()

                        .requestMatchers(
                                "/error",
                                "/actuator/health",
                                "/actuator/info",
                                "/actuator/prometheus"
                        )
                        .permitAll()

                        // Only employees/admins may list every notification
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/notifications"
                        )
                        .hasAnyRole(
                                "EMPLOYEE",
                                "ADMIN"
                        )

                        // Remaining notification APIs require authentication.
                        // Ownership is checked inside the service.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/notifications/**"
                        )
                        .hasAnyRole(
                                "CUSTOMER",
                                "EMPLOYEE",
                                "ADMIN"
                        )

                        .anyRequest()
                        .authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                );

        return http.build();
    }
}