package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.exceptions.NotPhotographerException;
import com.jemigraph.jemigraph_backend.models.LocationUpdateRequest;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LiveStatusService {

    private static final Logger log = LoggerFactory.getLogger(LiveStatusService.class);
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REDIS_GEO_KEY = "PHOTOGRAPHER_RADAR";

    public LiveStatusService(UserRepository userRepository, RedisTemplate<String, Object> redisTemplate) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public Map<String, Object> toggleStatus(String email, LocationUpdateRequest request) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!"PHOTOGRAPHER".equals(user.getRole().toString())) {
            throw new NotPhotographerException("Only photographer can go live!");
        }
        if (request.isOnline()) {
            if (request.getLongitude() == null || request.getLatitude() == null) {
                throw new IllegalArgumentException("Longitude and Latitude are required to go LIVE");
            }
        }
        String key = "photographer:online:" + user.getId();
        try {
            if (request.isOnline()) {
                String location = request.getLongitude() + "," + request.getLatitude();
                redisTemplate.opsForValue().set(key, location, Duration.ofSeconds(35));
                redisTemplate.opsForGeo().add(
                        REDIS_GEO_KEY,
                        new Point(request.getLongitude(), request.getLatitude()),
                        user.getId().toString()
                );
                log.info("✅ User {} is now LIVE at {}", user.getEmail(), location);
            } else {
                redisTemplate.delete(key);
                redisTemplate.opsForGeo().remove(REDIS_GEO_KEY, user.getId().toString());
                log.info("📴 User {} is now OFFLINE and removed from Redis", user.getEmail());
            }
        } catch (Exception e) {
            log.error("❌ Redis sync failed for user {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to update live status in cache", e);
        }

        user.setIsOnline(request.isOnline());
        userRepository.save(user);

        return Map.of(
                "email", email,
                "isOnline", user.getIsOnline(),
                "status", request.isOnline() ? "LIVE" : "OFFLINE"
        );
    }


    public List<Map<String, Object>> getNearbyPhotographers(double lat, double lng) {
      log.info(String.valueOf(lat),lng);

        Circle circle = new Circle(new Point(lng, lat), new Distance(7, Metrics.KILOMETERS));

        var results = redisTemplate.opsForGeo().radius(
                REDIS_GEO_KEY,
                circle,
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance()
        );
        if (results == null || results.getContent().isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> uuidList = results.getContent().stream()
                .map(result -> {
                    try {
                        String idStr = result.getContent().getName().toString().trim();
                        return UUID.fromString(idStr);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (uuidList.isEmpty()) return Collections.emptyList();

        List<User> users = userRepository.findAllByIdIn(uuidList);
        Map<UUID, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return results.getContent().stream()
                .map(result -> {
                    String idStr = result.getContent().getName().toString().trim();
                    User user = userMap.get(UUID.fromString(idStr));

                    if (user == null) return null;

                    String distanceDisplay = getString(result);

                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("userId", idStr);
                    map.put("distanceKm", Math.round(result.getDistance().getValue() * 100.0) / 100.0);
                    map.put("name", user.getName());
                    map.put("profileImage", user.getProfileImageUrl());
                    map.put("distance", distanceDisplay);
                    map.put("isOnline", user.getIsOnline());
                    map.put("rating", user.getAverageRating() != null ? user.getAverageRating() : 0.0);
                    return map;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static @NonNull String getString(GeoResult<RedisGeoCommands.GeoLocation<Object>> result) {
        double distanceInKm = result.getDistance().getValue(); // Hii inarudi kwa Kilomita
        String distanceDisplay;

        if (distanceInKm < 1.0) {
            // Ikiwa chini ya 1km, badilisha kuwa mita (x 1000)
            int meters = (int) Math.round(distanceInKm * 1000);
            distanceDisplay = meters + "m";
        } else {

            double roundedKm = Math.round(distanceInKm * 100.0) / 100.0;
            distanceDisplay = roundedKm + "km";
        }
        return distanceDisplay;
    }


    @Transactional
    public Map<String, Object> renewHeartbeat(String email, LocationUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!Boolean.TRUE.equals(user.getIsOnline())) {
            throw new IllegalStateException("User is not currently LIVE. Cannot renew heartbeat.");
        }

        if (request.getLongitude() == null || request.getLatitude() == null) {
            throw new IllegalArgumentException("Longitude and Latitude are required to renew live session");
        }

        String redisKey = "photographer:online:" + user.getId();
        String location = request.getLongitude() + "," + request.getLatitude();

        try {
            redisTemplate.opsForValue().set(redisKey, location, Duration.ofSeconds(35));
            redisTemplate.opsForGeo().add(
                    REDIS_GEO_KEY,
                    new Point(request.getLongitude(), request.getLatitude()),
                    user.getId().toString()
            );
            log.info("❤️ Heartbeat renewed for user {} at {}", user.getEmail(), location);
        } catch (Exception e) {
            log.error("❌ Heartbeat failed for user {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to renew live session in cache", e);
        }
        return Map.of(
                "email", email,
                "status", "LIVE_EXTENDED",
                "ttl_seconds", 35,
                "message", "Heartbeat renewed successfully. Live session extended."
        );
    }
@Transactional
    public void handleExpiredLiveSession(String userId) {
        try {
            User user = userRepository.findById(UUID.fromString(userId))
                    .orElse(null);

            if (user == null) return;

            if (Boolean.FALSE.equals(user.getIsOnline())) return;

            user.setIsOnline(false);
            userRepository.save(user);
            redisTemplate.delete("photographer:online:" + userId);
            redisTemplate.opsForGeo().remove(REDIS_GEO_KEY, userId);
            log.info("✅ User {} set OFFLINE due to expired Redis key.", user.getEmail());
        } catch (Exception e) {
            log.error("❌ Failed to handle expired session for user {}: {}", userId, e.getMessage());
        }
    }


}