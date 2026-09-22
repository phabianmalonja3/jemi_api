package com.jemigraph.jemigraph_backend.repositories;

import com.jemigraph.jemigraph_backend.Entities.Bookings;
import com.jemigraph.jemigraph_backend.enums.BookingStatus;
import io.lettuce.core.dynamic.annotation.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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

    @Query("SELECT COUNT(b) > 0 FROM Bookings b WHERE b.photographer.id = :photographerId " +
            "AND b.status NOT IN ('CANCELLED', 'COMPLETED') " +
            "AND ((b.pickupTime < :endTime) AND (b.endTime > :startTime))")
    boolean existsConflictingBooking(
            @Param("photographerId") UUID photographerId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}