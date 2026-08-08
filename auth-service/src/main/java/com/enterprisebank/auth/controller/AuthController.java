package com.enterprisebank.auth.controller;

import com.enterprisebank.auth.dto.LoginRequest;
import com.enterprisebank.auth.dto.LoginResponse;
import com.enterprisebank.auth.dto.RegisterRequest;
import com.enterprisebank.auth.dto.RegisterResponse;
import com.enterprisebank.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Protected Resources",
        description = "Authenticated user operations"
)
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

    @Operation(
            summary = "Authenticate a user",
            description = "Returns a JWT access token"
    )
    @ApiResponses({

            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful"
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid username or password"
            )
    })
    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }
}