package com.jemigraph.jemigraph_backend.controllers;

import com.jemigraph.jemigraph_backend.DTO.LocationUpdate;
import com.jemigraph.jemigraph_backend.Entities.Bookings;
import com.jemigraph.jemigraph_backend.enums.BookingStatus;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.services.impl.BookingServiceImp;
import com.jemigraph.jemigraph_backend.repositories.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TrackingController {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BookingServiceImp bookingService;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    private static final String GEO_KEY = "tracking:geo";

    @MessageMapping("/photographer/move")
    public void handleLocationUpdate(LocationUpdate payload) {
        if (payload == null || payload.getBookingId() == null || payload.getPhotographerId() == null) {
            log.warn("⚠️ Received incomplete tracking payload");
            return;
        }

        try {
            String bookingId = payload.getBookingId().trim();
            String photographerId = payload.getPhotographerId().trim();


            Bookings booking = bookingRepository.findById(UUID.fromString(bookingId))
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

            String currentStatus = booking.getStatus().name();

            if (booking.getStatus() == BookingStatus.IN_PROGRESS || booking.getStatus() == BookingStatus.COMPLETED) {
                broadcast(payload, currentStatus, 0.0, "Active");
                return;
            }

            // 2. Manage Redis Geospatial Data
            String photographerMember = "photographer:" + photographerId;
            String destinationMember = "destination:" + bookingId;
            ensureDestinationInRedis(bookingId);

            redisTemplate.opsForGeo().add(
                    GEO_KEY,
                    new Point(payload.getLng(), payload.getLat()),
                    photographerMember
            );

            // 3. Distance Calculation
            Distance distance = redisTemplate.opsForGeo().distance(
                    GEO_KEY,
                    photographerMember,
                    destinationMember,
                    RedisGeoCommands.DistanceUnit.METERS
            );

            double distanceValue = (distance != null) ? distance.getValue() : 0.0;
            String displayDistance = (distance != null) ? Math.round(distanceValue) + "m" : "Calculating...";

            // 4. GPS TRIGGER: If within 150m and status is still EN_ROUTE
            if (distance != null && distanceValue <= 150 && booking.getStatus() == BookingStatus.EN_ROUTE) {
                var user = userRepository.findById(UUID.fromString(photographerId))
                        .orElseThrow(() -> new RuntimeException("Photographer not found ID: " + photographerId));
       currentStatus = BookingStatus.ARRIVED.name();


                redisTemplate.opsForGeo().remove(GEO_KEY, photographerMember, destinationMember);
                log.info("📍 Photographer {} arrived for booking {}. Waiting for deposit.", photographerId, bookingId);
            }

            // 5. Final Broadcast
            broadcast(payload, currentStatus, distanceValue, displayDistance);

        } catch (Exception e) {
            log.error("❌ Tracking Error for booking {}: {}", payload.getBookingId(), e.getMessage());
        }
    }

    private void broadcast(LocationUpdate payload, String status, double distVal, String distLabel) {
        Map<String, Object> response = new HashMap<>();
        response.put("lat", payload.getLat());
        response.put("lng", payload.getLng());
        response.put("status", status);
        response.put("bookingId", payload.getBookingId());
        response.put("distance", distLabel);
        response.put("distanceMeters", distVal);

        messagingTemplate.convertAndSend("/topic/booking/" + payload.getBookingId(), Optional.of(response));
    }

    private void ensureDestinationInRedis(String bookingId) {
        String destinationMember = "destination:" + bookingId;
        var pos = redisTemplate.opsForGeo().position(GEO_KEY, destinationMember);

//        if (pos == null || pos.isEmpty() || pos.getFirst() == null) {
//            log.info("🔍 Redis cache miss. Syncing destination for Booking: {}", bookingId);
//            try {
//                Optional<Bookings> bookingOpt = bookingRepository.findById(UUID.fromString(bookingId));
//                if (bookingOpt.isPresent()) {
//                    Bookings b = bookingOpt.get();
//                    if (b.getDestinationLng() != null && b.getDestinationLat() != null) {
//                        redisTemplate.opsForGeo().add(
//                                GEO_KEY,
//                                new Point(b.getDestinationLng(), b.getDestinationLat()),
//                                destinationMember
//                        );
//                        log.info("✅ Destination SYNCED to Redis");
//                    }
//                }
//            } catch (Exception e) {
//                log.error("❌ Error syncing destination: {}", e.getMessage());
//            }
//        }
    }
}