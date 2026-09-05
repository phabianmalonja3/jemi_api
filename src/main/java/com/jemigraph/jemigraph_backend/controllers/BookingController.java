package com.jemigraph.jemigraph_backend.controllers;

import com.jemigraph.jemigraph_backend.DTO.BookingDTO;
import com.jemigraph.jemigraph_backend.DTO.BookingResponseDTO;
import com.jemigraph.jemigraph_backend.Entities.Bookings;
import com.jemigraph.jemigraph_backend.enums.BookingStatus;
import com.jemigraph.jemigraph_backend.mappers.BookingMapper;
import com.jemigraph.jemigraph_backend.mappers.BookingResponseMapper;
import com.jemigraph.jemigraph_backend.services.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;
    private final ApplicationEventPublisher eventPublisher;
    private final BookingMapper bookingMapper;
    private final BookingResponseMapper mapper;

    @PostMapping
    public ResponseEntity<?> createBooking(
            @Valid @RequestBody BookingDTO bookingDTO,
            Principal principal) {
            var savedBooking = bookingService.saveBooking(bookingDTO, principal.getName());
            return new ResponseEntity<>(savedBooking, HttpStatus.CREATED);

    }

    @GetMapping("/me")
    public ResponseEntity<List<BookingDTO>> getClientBooking(
            Principal principal){

        String userEmail = principal.getName();
        return ResponseEntity.ok(bookingService.getBookingsByClientEmail(userEmail));
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<List<BookingDTO>> getPhotographerBookings(Principal principal) {
        String userEmail = principal.getName();
        return ResponseEntity.ok(bookingService.getBookingsByPhotographerEmail(userEmail));
    }


    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> getBookingStatus(@PathVariable UUID id) {
        Bookings booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(mapper.toResponseDTO(booking));
    }

    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    @PostMapping("/{id}/accept")
    public ResponseEntity<BookingResponseDTO> acceptBooking(@PathVariable UUID id) {
        Bookings updatedBooking = bookingService.updateBookingStatus(id, BookingStatus.ACCEPTED);
        BookingResponseDTO dto = mapper.toResponseDTO(updatedBooking);
        return ResponseEntity.ok(dto);
    }


    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingResponseDTO> cancelBooking(@PathVariable UUID id) {
        Bookings updatedBooking = bookingService.updateBookingStatus(id, BookingStatus.CANCELLED);

        return ResponseEntity.ok(mapper.toResponseDTO(updatedBooking));
    }

    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    @PostMapping("/{id}/start-journey")
    public ResponseEntity<BookingResponseDTO> startJourney(@PathVariable UUID id) {
        Bookings updatedBooking = bookingService.updateBookingStatus(id, BookingStatus.EN_ROUTE);
        return ResponseEntity.ok(mapper.toResponseDTO(updatedBooking));
    }


    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    @PostMapping("/{id}/complete")
    public ResponseEntity<BookingResponseDTO> completeBooking(@PathVariable UUID id) {
        Bookings updatedBooking = bookingService.updateBookingStatus(id, BookingStatus.COMPLETED);
        return ResponseEntity.ok(mapper.toResponseDTO(updatedBooking));
    }

    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    @PostMapping("/{id}/status")
    public ResponseEntity<BookingResponseDTO> updateStatus(
            @PathVariable UUID id,
            @RequestParam BookingStatus status) {
        Bookings updatedBooking = bookingService.updateBookingStatus(id, status);
        return ResponseEntity.ok(mapper.toResponseDTO(updatedBooking));
    }



    @GetMapping("/active")
    public ResponseEntity<?> getActiveBooking(Principal principal) {
        Optional<Bookings> activeBooking = bookingService.getActiveBookingForClient(principal.getName());

       log.info(activeBooking.toString());
        if (activeBooking.isPresent()) {
            return ResponseEntity.ok(bookingMapper.toDto(activeBooking.get()));
        } else {
            return ResponseEntity.noContent().build();
        }
    }
}