package com.jemigraph.jemigraph_backend.responses;

import com.jemigraph.jemigraph_backend.Entities.User;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String name;
    private String lastName;
    private boolean isVerified;
    private String email;
    private String role;
}
