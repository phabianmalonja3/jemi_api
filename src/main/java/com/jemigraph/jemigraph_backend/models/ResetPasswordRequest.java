package com.jemigraph.jemigraph_backend.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {

  @NotBlank(message = "Token is required")
  private String email;

  @NotBlank(message = "New password is required")
  @Size(min = 6, message = "Password must be at least 8 characters long")
  private String newPassword;
}
