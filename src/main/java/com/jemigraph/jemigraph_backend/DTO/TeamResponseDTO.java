package com.jemigraph.jemigraph_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponseDTO{
    private UUID id;
    private String name;
    private String role;
    private String bio;
    private String imageUrl;
    private String instagramUrl;
    private String twitterUrl;
    private Integer displayOrder;
}