package com.jemigraph.jemigraph_backend.responses;

import com.jemigraph.jemigraph_backend.Entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor

@Data
@Builder

public class AuthentificationResponse {
    private User user;
    private String token;

}
