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
public class RegisterRequestDTO {
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "ADMIN|PHOTOGRAPHER|CLIENT", message = "Role must be ADMIN, PHOTOGRAPHER, or CLIENT")
    private String role;

    @Size(max = 20, message = "Phone number must be at most 20 characters")
    @Pattern(regexp = "^[0-9+\\-() ]*$", message = "Phone number contains invalid characters")
    private String phone;

    @Size(max = 255, message = "Location must be at most 255 characters")
    @Size(max = 5000, message = "Bio must be at most 5000 characters")
    private String bio;

    @Size(max = 100, message = "Display name must be at most 100 characters")
    private String displayName;
    private String fcmToken;

    private LocalDateTime subscriptionExpiresAt;
}
