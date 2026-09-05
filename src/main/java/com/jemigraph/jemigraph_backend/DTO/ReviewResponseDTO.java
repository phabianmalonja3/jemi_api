package com.jemigraph.jemigraph_backend.DTO;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReviewResponseDTO {
    private UUID id;
    private int rating;
    private String comment;
    private String clientName;
    private String photographerName;
    private String bookingType;
    private Double photographerAverageRating; // Average ya jumla ya huyo photographer
    private Integer photographerTotalReviews;
    private LocalDateTime createdAt;
}