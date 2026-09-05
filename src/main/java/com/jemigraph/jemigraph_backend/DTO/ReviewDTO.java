package com.jemigraph.jemigraph_backend.DTO;

import lombok.Data;

@Data
public class ReviewDTO {
    private int rating;
    private String comment;
    private String bookingId;
}
