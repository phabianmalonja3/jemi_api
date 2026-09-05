package com.jemigraph.jemigraph_backend.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordUpdateDTO {

    @NotBlank(message = "Current password is required")
    private String oldPassword;

    @NotBlank(message = "New password cannot be blank")
    @Size(min = 6, message = "New password must be at least 6 characters long")
    private String newPassword;
}