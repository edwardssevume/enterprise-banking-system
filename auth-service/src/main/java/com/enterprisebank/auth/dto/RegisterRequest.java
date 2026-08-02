package com.enterprisebank.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(
            min = 3,
            max = 50,
            message = "Username must contain between 3 and 50 characters"
    )
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email address is not valid")
    @Size(
            max = 100,
            message = "Email must not exceed 100 characters"
    )
    private String email;

    @NotBlank(message = "Password is required")
    @Size(
            min = 8,
            max = 100,
            message = "Password must contain between 8 and 100 characters"
    )
    private String password;
}
