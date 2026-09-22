package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.DTO.BookingDTO;
import com.jemigraph.jemigraph_backend.DTO.BookingResponseDTO;
import com.jemigraph.jemigraph_backend.Entities.Bookings;
import com.jemigraph.jemigraph_backend.enums.BookingStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface BookingService {

    BookingDTO saveBooking(BookingDTO dto, String email);
   Bookings updateBookingStatus(UUID bookingId, BookingStatus newStatus);
    Bookings getBookingById(UUID bookingId);
    List<BookingDTO> getBookingsByPhotographerEmail(String email);
    Optional<Bookings> getActiveBookingForClient(String email);
    List<BookingDTO> getBookingsByClientEmail(String email);

    boolean checkAvailability(
            UUID photographerId,
            UUID packageId,
            LocalDateTime pickupTime);
    Page<BookingResponseDTO> getAllBookings(Pageable pageable);
}
