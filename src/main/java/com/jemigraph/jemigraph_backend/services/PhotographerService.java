package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.mappers.PhotographerMapper;
import com.jemigraph.jemigraph_backend.DTO.PhotographerProfileDTO;
import com.jemigraph.jemigraph_backend.Entities.PhotographerProfile;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.enums.UserRole;
import com.jemigraph.jemigraph_backend.repositories.PhotographerProfileRepository;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PhotographerService {

    private static final Logger log = LogManager.getLogger(PhotographerService.class);
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PhotographerProfileRepository profileRepository;
    private final PhotographerMapper photographerMappper;


    private static final String REDIS_GEO_KEY = "ACTIVE_PHOTOGRAPHERS";

    @Transactional
    public PhotographerProfile updateProfile(UUID userId, PhotographerProfile incomingData) {
        // 1. Fetch User (Managed by Hibernate)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // 2. Fetch existing profile or initialize
        PhotographerProfile profile = profileRepository.findById(userId)
                .orElseGet(() -> {
                    PhotographerProfile newP = new PhotographerProfile();

                    // --- THE CRITICAL FIX ---
                    newP.setUser(user);    // Link Profile -> User
//                    user.setProfile(newP); // Link User -> Profile (Bi-directional)
                    // ------------------------

                    newP.setUserId(userId);
                    return newP;
                });

        // Ensure links are active for existing profiles too
        profile.setUser(user);
        profile.setUserId(userId);

        // 3. Manual Mapping (Clean approach)
        mapProfileData(incomingData, profile);

        // 4. Save
        // Tip: Since we linked bi-directionally, saving the profile or the user works.
        return profileRepository.save(profile);
    }

    private void mapProfileData(PhotographerProfile source, PhotographerProfile target) {
        if (source.getRole() != null) target.setRole(source.getRole());
        if (source.getSpecialty() != null) target.setSpecialty(source.getSpecialty());
        if (source.getLocation() != null) target.setLocation(source.getLocation());
        if (source.getExperience() != null) target.setExperience(source.getExperience());
        if (source.getBio() != null) target.setBio(source.getBio());
        if (source.getQuote() != null) target.setQuote(source.getQuote());
        if (source.getImage() != null) target.setImage(source.getImage());
        if (source.getAvailable() != null) target.setAvailable(source.getAvailable());
        if (source.getSocial() != null) target.setSocial(source.getSocial());

        // Collections
        if (source.getGallery() != null) {
            target.getGallery().clear();
            target.getGallery().addAll(source.getGallery());
        }
        if (source.getAchievements() != null) {
            target.getAchievements().clear();
            target.getAchievements().addAll(source.getAchievements());
        }
    }




    public PhotographerProfileDTO getPhotographerById(UUID id) {
        return userRepository.findByIdAndRole(id, UserRole.PHOTOGRAPHER)
                .map(photographerMappper::toDto)
                .orElseThrow(() -> new RuntimeException("Photographer not found with ID: " + id));
    }


    public List<Object> getNearbyPhotographers(double lat, double lng) {
        String key = "photographer:online";


        Circle within = new Circle(new Point(lng, lat), new Distance(7, Metrics.KILOMETERS));

        GeoResults<RedisGeoCommands.GeoLocation<Object>> results = redisTemplate.opsForGeo().radius(key, within);


        log.info(results);
        return results.getContent().stream()
                .map(result -> result.getContent().getName())
                .collect(Collectors.toList());
    }
}