package com.enterprisebank.auth.controller;

import com.enterprisebank.auth.dto.LoginRequest;
import com.enterprisebank.auth.dto.LoginResponse;
import com.enterprisebank.auth.dto.RegisterRequest;
import com.enterprisebank.auth.dto.RegisterResponse;
import com.enterprisebank.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }
}