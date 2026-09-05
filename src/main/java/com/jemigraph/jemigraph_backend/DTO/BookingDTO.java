package com.jemigraph.jemigraph_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private UUID id;
    private UUID photographerId;
    private String photographerName;
    private UUID clientId;
    private String clientName;
    private UUID packageId;
    private String packageName;
    private String type;
    private LocalDateTime pickupTime;
    private String addressName;
    private Double lat;
    private Double lng;
    private BigDecimal amountPaid;
    private String status;
    private String paymentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}