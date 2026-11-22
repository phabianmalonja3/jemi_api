package com.jemigraph.jemigraph_backend.controllers;

import com.jemigraph.jemigraph_backend.DTO.BookingResponseDTO;
import com.jemigraph.jemigraph_backend.services.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SalesController {
	private final BookingService bookingService;
	@GetMapping("/bookings")
	public ResponseEntity<Page<BookingResponseDTO>> getAllBookings(Pageable pageable) {
		Page<BookingResponseDTO> bookings = bookingService.getAllBookings(pageable);
		return ResponseEntity .ok(bookings);
	}
}
