package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.Entities.PhotographerProfile;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.repositories.PhotographerProfileRepository;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotographerProfileService {

    private final PhotographerProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Transactional
    public PhotographerProfile updateProfile(UUID userId, PhotographerProfile updateData) {
        // 1. Fetch the User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // 2. Get existing profile or create a new one if it doesn't exist
        PhotographerProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    updateData.setUser(user);
                    updateData.setUserId(userId); // Share the UUID
                    return updateData;
                });

        // 3. Update basic fields (only if provided)
        if (updateData.getRole() != null) profile.setRole(updateData.getRole());
        if (updateData.getSpecialty() != null) profile.setSpecialty(updateData.getSpecialty());
        if (updateData.getLocation() != null) profile.setLocation(updateData.getLocation());
        if (updateData.getExperience() != null) profile.setExperience(updateData.getExperience());
        if (updateData.getBio() != null) profile.setBio(updateData.getBio());
        if (updateData.getQuote() != null) profile.setQuote(updateData.getQuote());
        if (updateData.getImage() != null) profile.setImage(updateData.getImage());
        if (updateData.getAvailable() != null) profile.setAvailable(updateData.getAvailable());

        // 4. Update Collections
        if (updateData.getGallery() != null) {
            profile.getGallery().clear();
            profile.getGallery().addAll(updateData.getGallery());
        }

        if (updateData.getAchievements() != null) {
            profile.getAchievements().clear();
            profile.getAchievements().addAll(updateData.getAchievements());
        }

        // 5. Update Social Links (Embedded)
        if (updateData.getSocial() != null) {
            profile.setSocial(updateData.getSocial());
        }

        return profileRepository.save(profile);
    }
}