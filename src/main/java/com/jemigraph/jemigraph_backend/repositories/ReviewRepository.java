package com.jemigraph.jemigraph_backend.repositories;

import com.jemigraph.jemigraph_backend.events.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByPhotographerId(UUID photographerId);
    Optional<Review> findByBookingId(UUID bookingId);
    List<Review> findByClientId(UUID clientId);

}
