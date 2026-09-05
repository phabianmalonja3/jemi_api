package com.jemigraph.jemigraph_backend.requests;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data

@RequiredArgsConstructor

public class UserRegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 32, message = "Password must be between 8 and 32 characters")
    private String password;

    @NotBlank(message = "Phone number is required")
    @Size(min = 10, max = 13, message = "Phone number must be between 10 and 13 digits")
    private String phoneNumber;

    @NotBlank(message = "Name is required")
    private String name;

}