package com.enterprisebank.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class ProtectedController {

    @GetMapping("/me")
    public Map<String, Object> currentUser(
            Authentication authentication
    ) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put(
                "username",
                authentication.getName()
        );

        response.put(
                "authorities",
                authentication.getAuthorities()
                        .stream()
                        .map(authority -> authority.getAuthority())
                        .collect(Collectors.toList())
        );

        return response;
    }

    @GetMapping("/admin/dashboard")
    public Map<String, String> adminDashboard() {
        return Map.of(
                "message",
                "Welcome to the administrator dashboard"
        );
    }
}