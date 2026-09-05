package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.DTO.NearbyPhotographerResponse;
import com.jemigraph.jemigraph_backend.repositories.UserRepository; // Hakikisha ume-import hii
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository; // Tutaivuta DB ili kupata Jina la mtu
    private static final String REDIS_GEO_KEY = "ACTIVE_PHOTOGRAPHERS";

    public List<NearbyPhotographerResponse> findNearby(double clientLat, double clientLon, double radiusKm) {

        // 1. Kutafuta kwenye Redis (Geospatial search)
        Circle area = new Circle(new Point(clientLon, clientLat), new Distance(radiusKm, RedisGeoCommands.DistanceUnit.KILOMETERS));

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeDistance()
                .includeCoordinates() // Hii inatusaidia kupata lat/lon moja kwa moja kutoka Redis
                .sortAscending();

        GeoResults<RedisGeoCommands.GeoLocation<Object>> results = redisTemplate.opsForGeo().radius(REDIS_GEO_KEY, area, args);

        if (results == null) return List.of();

        return results.getContent().stream().map(result -> {
            String email = (String) result.getContent().getName();
            double dist = result.getDistance().getValue();

            // 2. ETA Calculation (Kadirio la Muda)
            // Kasi ya wastani ya usafiri Dar ni kama 20km/h
            int minutes = (int) Math.ceil((dist / 20) * 60) + 2;

            // 3. Vuta jina kutoka Database (Optional lakini Muhimu kwa Mteja)
            var user = userRepository.findByEmail(email).orElse(null);
            String fullName = (user != null) ? user.getName() : "Photographer";

            // HAPA NDIPO PALIKUWA NA KOSA (Tumia class name badala ya DoubleStream)
            return NearbyPhotographerResponse.builder()
                    .email(email)
                    .name(fullName)
                    .distance(String.format("%.1f km", dist))
                    .expectedTime(minutes + " mins")
                    .latitude(result.getContent().getPoint().getY())
                    .longitude(result.getContent().getPoint().getX())
                    .build();
        }).collect(Collectors.toList());
    }
}