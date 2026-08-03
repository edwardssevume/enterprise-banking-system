package com.enterprisebank.auth.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long expirationMilliseconds;
    private final String issuer;

    public JwtService(
            JwtEncoder jwtEncoder,

            @Value("${security.jwt.expiration}")
            long expirationMilliseconds,

            @Value("${security.jwt.issuer}")
            String issuer
    ) {
        this.jwtEncoder = jwtEncoder;
        this.expirationMilliseconds = expirationMilliseconds;
        this.issuer = issuer;
    }

    public String generateToken(
            Authentication authentication,
            Long userId
    ) {

        Instant issuedAt = Instant.now();

        Instant expiresAt = issuedAt.plusMillis(
                expirationMilliseconds
        );

        List<String> roles = authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(authentication.getName())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .claim("userId", userId)
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }

    public long getExpirationSeconds() {
        return expirationMilliseconds / 1000;
    }
}