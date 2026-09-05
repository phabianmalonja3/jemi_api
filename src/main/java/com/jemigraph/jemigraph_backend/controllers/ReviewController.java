package com.jemigraph.jemigraph_backend.controllers;

import com.jemigraph.jemigraph_backend.DTO.ReviewDTO;
import com.jemigraph.jemigraph_backend.DTO.ReviewResponseDTO;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    @GetMapping
    public ResponseEntity<List<ReviewResponseDTO>> getAllReviews() {
        return ResponseEntity.ok(reviewService.findAllReviews());
    }
    @PostMapping("/{bookingId}")
    public ResponseEntity<?> submitReview(
            @PathVariable UUID bookingId,
            @RequestBody ReviewDTO request,
            Principal principal) {

            ReviewResponseDTO createdReview = reviewService.createReview(bookingId, request,principal.getName() );
            return ResponseEntity.ok(createdReview);

    }

    @GetMapping("/my-reviews")
    public ResponseEntity<?> getMyReviews(@AuthenticationPrincipal User photographer) {

        return ResponseEntity.ok(reviewService.findByPhotographerId(photographer.getId()));
    }
}
