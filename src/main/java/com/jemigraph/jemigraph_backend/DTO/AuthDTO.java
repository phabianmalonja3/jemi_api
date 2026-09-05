package com.jemigraph.jemigraph_backend.DTO;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class AuthDTO {
    private UUID id;
    private String name;
    private String email;
    private String role;
    private boolean isOnline;
    private boolean isBusy;
    private Double rating;
    private List<Map<String, String>> authorities;
}


