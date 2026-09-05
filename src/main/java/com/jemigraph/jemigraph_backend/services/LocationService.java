package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.DTO.LocationResponseDTO;
import com.jemigraph.jemigraph_backend.Entities.Location;
import com.jemigraph.jemigraph_backend.Entities.LocationRequestDTO;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.mappers.LocationMapper;
import com.jemigraph.jemigraph_backend.repositories.LocationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final LocationRepository locationRepository;
    private static final String TRACKING_KEY = "PHOTOGRAPHER_LOCATIONS";
    private final LocationMapper locationMapper;
    // 1. Mpiga picha anapowasha "Go Live" au anapotembea
    public void updateLocation(String photographerEmail, double lat, double lon) {
        // Redis inatumia (Longitude, Latitude) - Point(x, y)
        redisTemplate.opsForGeo().add(
                TRACKING_KEY,
                new Point(lon, lat),
                photographerEmail
        );
    }

    // 2. Mteja anapotafuta wapiga picha ndani ya km 5
    public GeoResults<RedisGeoCommands.GeoLocation<Object>> getNearby(double lat, double lon, double radiusKm) {
        Circle area = new Circle(new Point(lon, lat), new Distance(radiusKm, RedisGeoCommands.DistanceUnit.KILOMETERS));
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs().includeDistance().sortAscending();

        return redisTemplate.opsForGeo().radius(TRACKING_KEY, area, args);
    }



    @Transactional
    public LocationResponseDTO updateUserLocation(User user, LocationRequestDTO dto) {

        Location location = locationRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Location newLocation = new Location();
                    newLocation.setUser(user);
                    return newLocation;
                });


        location.setLatitude(dto.getLatitude());
        location.setLongitude(dto.getLongitude());
        location.setAddress(dto.getAddress());




        return locationMapper.toDtoResponse(locationRepository.save(location));
    }

    public LocationResponseDTO getUserLocation(User user) {
        Location location = locationRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Location not set for this user"));


        return locationMapper.toDtoResponse(location);
    }


}