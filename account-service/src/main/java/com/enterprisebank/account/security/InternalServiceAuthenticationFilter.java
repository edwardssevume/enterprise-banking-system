package com.enterprisebank.account.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class InternalServiceAuthenticationFilter
        extends OncePerRequestFilter {

    private static final String HEADER_NAME =
            "X-Internal-Service-Key";

    private final String expectedSecret;

    public InternalServiceAuthenticationFilter(
            @Value("${internal.service.secret}")
            String expectedSecret
    ) {
        this.expectedSecret = expectedSecret;
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {
        return !request.getRequestURI()
                .startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String providedSecret =
                request.getHeader(HEADER_NAME);

        if (providedSecret == null
                || !expectedSecret.equals(providedSecret)) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "transaction-service",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_INTERNAL_SERVICE"
                                )
                        )
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(
                request,
                response
        );
    }
}