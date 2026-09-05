package com.jemigraph.jemigraph_backend.events;

import com.jemigraph.jemigraph_backend.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentEvent(UUID bookingId,
                           UUID photographerId,
                           BigDecimal amount,
                           PaymentStatus status ){// PARTIALLY_PAID au FULLY_PAID) {
}
