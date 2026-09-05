package com.jemigraph.jemigraph_backend.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PasswordForgotDTO {

    @NotNull(message = "Email required !")
    private String email;
}
