package com.jemigraph.jemigraph_backend.DTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReviewSummaryDTO {
    private UUID id;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}