package com.jemigraph.jemigraph_backend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jemigraph.jemigraph_backend.enums.SubscriptionStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RegistrationResponseDTO {
    private UUID id;
    private Boolean isVerified;
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    @Size(min = 6, message = "Password must be at least 6 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "ADMIN|PHOTOGRAPHER|CLIENT", message = "Role must be ADMIN or PHOTOGRAPHER")
    private String role;
    private LocalDateTime registeredAt;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime trialEndsAt;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private SubscriptionStatus subscriptionStatus;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime subscriptionExpiresAt;
}