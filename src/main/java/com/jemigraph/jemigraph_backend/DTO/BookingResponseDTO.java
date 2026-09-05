package com.jemigraph.jemigraph_backend.DTO;

import com.jemigraph.jemigraph_backend.DTO.PackageSummaryDTO;
import com.jemigraph.jemigraph_backend.DTO.ReviewSummaryDTO;
import com.jemigraph.jemigraph_backend.DTO.UserSummaryDTO;
import com.jemigraph.jemigraph_backend.enums.BookingStatus;
import com.jemigraph.jemigraph_backend.enums.BookingType;
import com.jemigraph.jemigraph_backend.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BookingResponseDTO {
    private UUID id;
    private BookingType type;
    private LocalDateTime pickupTime;
    private String addressName;
    private Double lat;
    private Double lng;
    private BigDecimal amountPaid;
    private BookingStatus status;
    private PaymentStatus paymentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserSummaryDTO photographer;
    private UserSummaryDTO client;
    private PackageSummaryDTO pkg;
    private ReviewSummaryDTO review;
}