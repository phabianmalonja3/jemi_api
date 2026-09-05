package com.jemigraph.jemigraph_backend.DTO;

import com.jemigraph.jemigraph_backend.enums.PackageLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class PkgDTO {
    private UUID id;
    private String name;
    private String duration;
    private Double price;
    private PackageLevel level;
    private List<String> features;
}
