package com.jemigraph.jemigraph_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatsDTO {
	private long totalBookings;
	private long confirmedBookings; // Au completed/success
	private long pendingBookings;
	private long cancelledBookings;
	private double successRate;
	private double cancellationRate;
}