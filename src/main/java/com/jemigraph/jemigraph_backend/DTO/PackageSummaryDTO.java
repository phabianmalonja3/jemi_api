package com.jemigraph.jemigraph_backend.DTO;

import lombok.Data;
import java.util.UUID;
import java.math.BigDecimal;

@Data
public class PackageSummaryDTO {
    private UUID id;
    private String name;
    private String description; // optional
    private BigDecimal price;
}