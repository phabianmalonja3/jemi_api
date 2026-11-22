package com.jemigraph.jemigraph_backend.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffDTO {

	private String id;

	@NotBlank(message = "Name is required")
	@Size(min = 3, max = 100)
	private String name;

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	private String email;

	@NotBlank(message = "Password is required")
	@Size(min = 8, message = "Password must be at least 8 characters")
	private String password;

	@NotBlank(message = "Role is required")
	@Pattern(
			regexp = "ADMIN|SALES",
			message = "Role must be ADMIN or SALES"
	)
	private String role;
}