package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.DTO.LiveLocationUpdate;
import com.jemigraph.jemigraph_backend.services.LiveLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LiveLocationImpl implements LiveLocationService {

        private final SimpMessagingTemplate messagingTemplate;
        private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<Object> getNearbyPhotographers(double lat, double lng) {
        Circle within = new Circle(new Point(lng, lat), new Distance(7, Metrics.KILOMETERS));

        String key = "photographers:online";
        GeoResults<RedisGeoCommands.GeoLocation<Object>> results =
                redisTemplate.opsForGeo().radius(key,within);

        return results.getContent().stream()
                .map(result -> result.getContent().getName())
                .collect(Collectors.toList());
    }

    public void updateLiveLocation(LiveLocationUpdate update) {
        String geoKey = "photographers:online";
        String heartbeatKey = "photographer:heartbeat:" + update.getUserId();
        redisTemplate.opsForGeo().add(
                geoKey,
                new Point(update.getLongitude(), update.getLatitude()),
                update.getUserId()
        );
        redisTemplate.opsForValue().set(heartbeatKey, "active", Duration.ofSeconds(35));
        messagingTemplate.convertAndSend("/topic/tracking/" + update.getBookingId(), update);
    }
}

