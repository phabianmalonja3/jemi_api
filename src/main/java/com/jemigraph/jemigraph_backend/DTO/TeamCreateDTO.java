package com.jemigraph.jemigraph_backend.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamCreateDTO {

    @NotBlank(message = "Name is mandatory")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Role is mandatory")
    @Size(max = 150, message = "Role cannot exceed 150 characters")
    private String role;

    @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
    private String bio;

    private String imageUrl;

    private String instagramUrl;

    private String twitterUrl;

    private Integer displayOrder;
}