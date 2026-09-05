package com.jemigraph.jemigraph_backend.repositories;

import com.jemigraph.jemigraph_backend.Entities.Bookings;
import com.jemigraph.jemigraph_backend.enums.BookingStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Bookings, UUID> {
    List<Bookings> findByPhotographerId(UUID photographerId);
    Page<Bookings> findAllByClientId(UUID id, Pageable pageable);
    List<Bookings> findByClientId(UUID clientId);
    List<Bookings> findByPhotographerEmail(String email);
    Optional<Bookings> findTopByClientIdAndStatusNotInOrderByCreatedAtDesc(
            UUID clientId,
            List<BookingStatus> statuses
    );
    boolean existsByPhotographerIdAndStatusIn(UUID photographerId, List<BookingStatus> accepted);
}