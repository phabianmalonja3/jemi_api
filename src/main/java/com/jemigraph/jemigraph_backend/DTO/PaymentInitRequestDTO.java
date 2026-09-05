package com.jemigraph.jemigraph_backend.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitRequestDTO {

    @NotNull(message = "Plan ID is required")
    private UUID planId;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^255[0-9]{9}$",
            message = "Phone number must be in international format (e.g., 2557XXXXXXXX)"
    )
    private String phoneNumber;
}
