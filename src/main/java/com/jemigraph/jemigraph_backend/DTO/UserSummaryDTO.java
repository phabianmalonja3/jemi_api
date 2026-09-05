package com.jemigraph.jemigraph_backend.DTO;

import lombok.Data;
import java.util.UUID;

@Data
public class UserSummaryDTO {
    private UUID id;
    private String name;
    private String email;
    private String profileImageUrl; // optional
    // add any other fields you need (e.g., phone from profile)
}