package com.jemigraph.jemigraph_backend.events;


import com.jemigraph.jemigraph_backend.DTO.BookingDTO;

import java.util.UUID;


public record BookingCreatedEvent(
        UUID bookingId,
        UUID photographerId,
        String clientName,
        String photographerFcmToken,
        String photographerPhone,
        String clientAvatar,
        String formattedPickupTime,
        String addressName,
        String bookingType
) {}