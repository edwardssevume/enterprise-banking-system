package com.enterprisebank.audit.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

@Configuration
public class JwtAuthenticationConverterConfig {

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken>
    jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("roles");

        /*
         * Our JWT already contains authorities such as:
         *
         * ROLE_ADMIN
         * ROLE_EMPLOYEE
         * ROLE_CUSTOMER
         *
         * Therefore we do not want Spring adding another prefix.
         */
        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter delegate =
                new JwtAuthenticationConverter();

        delegate.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter
        );

        return new Converter<Jwt, AbstractAuthenticationToken>() {

            @Override
            public AbstractAuthenticationToken convert(Jwt jwt) {
                return delegate.convert(jwt);
            }
        };
    }
}