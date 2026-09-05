package com.jemigraph.jemigraph_backend.services;

 // Ensure you have this
import com.jemigraph.jemigraph_backend.exceptions.NoPhotographersAvailableException;
import com.jemigraph.jemigraph_backend.requests.BookingRequest;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration; // Ensure this is java.time.Duration
import java.util.Map;

@Service
public class DispatchService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final String REDIS_GEO_KEY = "PHOTOGRAPHER_RADAR";

    public DispatchService(RedisTemplate<String, Object> redisTemplate, SimpMessagingTemplate messagingTemplate) {
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    public void dispatchRequest(BookingRequest booking) {
        // 1. Find nearby photographers within 5km
        // Use Distance and Metrics correctly
        Distance radius = new Distance(5, Metrics.KILOMETERS);
        Circle area = new Circle(new Point(booking.getLng(), booking.getLat()), radius);

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeDistance()
                .sortAscending();

        GeoResults<RedisGeoCommands.GeoLocation<Object>> nearby = redisTemplate.opsForGeo()
                .radius(REDIS_GEO_KEY, area, args);

        if (nearby == null || nearby.getContent().isEmpty()) {
            throw new NoPhotographersAvailableException("No photographers nearby in Dar es Salaam.");
        }

        // 2. Get the closest available photographer
        var closestMember = nearby.getContent().get(0).getContent().getName().toString();

        // 3. Send a private notification (The "Bolt" Shake)
        // Principle: messagingTemplate.convertAndSendToUser looks for the username (email)
        messagingTemplate.convertAndSendToUser(
                closestMember,
                "/queue/incoming-request",
                Map.of(
                        "bookingId", booking.getId(),
                        "clientName", booking.getClientName(),
                        "distance", nearby.getContent().get(0).getDistance().getValue()
                )
        );

        // 4. State Management: Set a 'Lock' in Redis
        // If photographer doesn't 'ACCEPT' within 20s, this key expires
        String lockKey = "PENDING_ASSIGNMENT:" + booking.getId();
        redisTemplate.opsForValue().set(lockKey, closestMember, Duration.ofSeconds(20));
    }
}