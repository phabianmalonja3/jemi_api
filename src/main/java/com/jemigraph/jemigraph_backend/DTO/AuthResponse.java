package com.jemigraph.jemigraph_backend.DTO;

import com.jemigraph.jemigraph_backend.Entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
private UserDTO user;
    private String accessToken;
    private String refreshToken;
}