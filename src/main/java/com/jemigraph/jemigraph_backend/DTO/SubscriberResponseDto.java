package com.jemigraph.jemigraph_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriberResponseDto {
    private UUID userId;
    private String email;
    private String subscriptionStatus;
    private LocalDateTime expiresAt;
    private String planName;
    private BigDecimal planAmount;
    private Integer durationInDays;
}