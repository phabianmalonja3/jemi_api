package com.jemigraph.jemigraph_backend.events;

import com.jemigraph.jemigraph_backend.enums.BookingStatus;
import java.util.UUID;

public record BookingStatusChangedEvent(
        UUID bookingId,
        UUID clientId,
        BookingStatus newStatus,
        String clientFcmToken,
        String clientPhone,
        String photographerName,
        String profileImage
) {}
