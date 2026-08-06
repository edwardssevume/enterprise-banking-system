package com.enterprisebank.customer.controller;

import com.enterprisebank.customer.dto.CreateCustomerRequest;
import com.enterprisebank.customer.dto.CustomerResponse;
import com.enterprisebank.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse createCustomer(
            @Valid @RequestBody CreateCustomerRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {

        Long authUserId = getAuthenticatedUserId(jwt);

        return customerService.createCustomer(
                request,
                authUserId
        );
    }

    @GetMapping("/me")
    public CustomerResponse getMyProfile(
            @AuthenticationPrincipal Jwt jwt
    ) {

        Long authUserId = getAuthenticatedUserId(jwt);

        return customerService.getCustomerByAuthUserId(
                authUserId
        );
    }

    @GetMapping("/{id}")
    public CustomerResponse getCustomerById(
            @PathVariable Long id
    ) {
        return customerService.getCustomerById(id);
    }

    private Long getAuthenticatedUserId(Jwt jwt) {

        Number userId = jwt.getClaim("userId");

        if (userId == null) {
            throw new IllegalStateException(
                    "JWT does not contain userId"
            );
        }

        return userId.longValue();
    }
}