package com.enterprisebank.auth.service;

import com.enterprisebank.auth.dto.LoginRequest;
import com.enterprisebank.auth.dto.LoginResponse;
import com.enterprisebank.auth.dto.RegisterRequest;
import com.enterprisebank.auth.dto.RegisterResponse;
import com.enterprisebank.auth.entity.Role;
import com.enterprisebank.auth.entity.RoleName;
import com.enterprisebank.auth.entity.User;
import com.enterprisebank.auth.exception.DuplicateResourceException;
import com.enterprisebank.auth.repository.RoleRepository;
import com.enterprisebank.auth.repository.UserRepository;
import com.enterprisebank.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        String username = request.getUsername().trim();

        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException(
                    "Username is already taken"
            );
        }

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException(
                    "Email is already registered"
            );
        }

        Role customerRole = roleRepository
                .findByName(RoleName.ROLE_CUSTOMER)
                .orElseThrow(() -> new IllegalStateException(
                        "ROLE_CUSTOMER has not been initialized"
                ));

        Set<Role> roles = new HashSet<>();
        roles.add(customerRole);

        User user = User.builder()
                .username(username)
                .email(email)
                .password(
                        passwordEncoder.encode(request.getPassword())
                )
                .enabled(true)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                "User registered successfully"
        );
    }

    public LoginResponse login(LoginRequest request) {

        String username = request.getUsername().trim();

        Authentication authenticationRequest =
                UsernamePasswordAuthenticationToken.unauthenticated(
                        username,
                        request.getPassword()
                );

        Authentication authentication =
                authenticationManager.authenticate(
                        authenticationRequest
                );

        User authenticatedUser = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user no longer exists"
                ));

        String accessToken = jwtService.generateToken(
                authentication,
                authenticatedUser.getId()
        );

        List<String> roles = authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new LoginResponse(
                accessToken,
                "Bearer",
                jwtService.getExpirationSeconds(),
                authentication.getName(),
                roles
        );
    }
}