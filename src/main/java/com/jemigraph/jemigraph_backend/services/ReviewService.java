package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.DTO.ReviewDTO;
import com.jemigraph.jemigraph_backend.DTO.ReviewResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface ReviewService {
    ReviewResponseDTO createReview(UUID bookingId, ReviewDTO request, String client);
    List<ReviewResponseDTO> findByPhotographerId(UUID id);

    List<ReviewResponseDTO> findAllReviews();
}
