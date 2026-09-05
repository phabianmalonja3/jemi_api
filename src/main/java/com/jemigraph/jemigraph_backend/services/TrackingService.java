package com.jemigraph.jemigraph_backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.geo.Metrics; // Hakikisha hii ipo
import org.springframework.data.redis.domain.geo.GeoReference; // Kwa Spring Boot 3+

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrackingService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String TRACKING_KEY = "photographer:locations";

    // 1. Inapokelewa kutoka kwa App ya Photographer
    public void updateLocation(UUID photographerId, Double lat, Double lng) {
        GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();

        // Hifadhi location ya sasa kwenye Redis Geo Index
        geoOps.add(TRACKING_KEY, new Point(lng, lat), photographerId.toString());
    }

    // 2. Inatumiwa na App ya Mteja kuona umbali
    public Distance getDistanceToClient(UUID photographerId, Double clientLat, Double clientLng) {
        GeoOperations<String, String> geoOps = redisTemplate.opsForGeo();

        // 1. Tengeneza Point ya mteja (Longitude kwanza, kisha Latitude)
        Point clientPoint = new Point(clientLng, clientLat);

        // 2. Piga hesabu ya umbali kati ya photographer (iliyopo Redis) na mteja (Point mpya)
        // Kumbuka: Spring Data Redis hutumia GeoReference kwa ajili ya kulinganisha Point na Member
        return geoOps.distance(
                TRACKING_KEY,
                photographerId.toString(),
                String.valueOf(clientPoint),
                Metrics.KILOMETERS
        );
    }
}