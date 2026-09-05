package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.DTO.ProfileUpdateDTO;
import com.jemigraph.jemigraph_backend.DTO.UserProfileResponse;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.Entities.UserProfile;
import com.jemigraph.jemigraph_backend.mappers.UserProfileMapper;
import com.jemigraph.jemigraph_backend.repositories.UserProfileRepository;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.services.UserProfileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    // Use @Value to make it configurable
    @Value("${upload.dir:uploads}")
    private String UPLOAD_DIR;

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final UserProfileMapper userProfileMapper;
    private final FileStorageService  fileStorageService;
    @Transactional
    @Override
    public UserProfile updateProfile(String userEmail, ProfileUpdateDTO dto) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            user.setName(dto.getName());
            user = userRepository.save(user);
        }
        User finalUser = user;
        UserProfile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> UserProfile.builder()
                        .user(finalUser)
                        .build());
        profile.setUser(finalUser);

        // Update profile data
        if (dto.getDisplayName() != null) profile.setDisplayName(dto.getDisplayName());
        if (dto.getPhone() != null) profile.setPhone(dto.getPhone());
        if (dto.getLocation() != null) profile.setLocation(dto.getLocation());
        if (dto.getBio() != null) profile.setBio(dto.getBio());
        if (dto.getInstagram() != null) profile.setInstagram(dto.getInstagram());
        if (dto.getFacebook() != null) profile.setFacebook(dto.getFacebook());
        if (dto.getTwitter() != null) profile.setTwitter(dto.getTwitter());
        if (dto.getLinkedin() != null) profile.setLinkedin(dto.getLinkedin());
        if (dto.getWebsite() != null) profile.setWebsite(dto.getWebsite());

        return profileRepository.save(profile);
    }

    @Transactional
    @Override
    public UserProfileResponse getProfileByEmail(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        UserProfile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Profile not found for user: " + user.getEmail()
                        )
                );
        return userProfileMapper.toResponse(profile);
    }


    @Override
    public String saveProfileImage(String email ,MultipartFile file) {
String dbPath = fileStorageService.storeProfile(file);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            UserProfile profile = profileRepository.findByUserId(user.getId())
                    .orElse(UserProfile.builder().user(user).build());
            profile.setProfileImage(dbPath);
            profileRepository.save(profile);
            return dbPath;

    }

    @Override
    public boolean deleteProfileImage(String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.isEmpty()) {
                return false;
            }

            // Extract filename from URL
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            Path path = Paths.get(UPLOAD_DIR + filename);

            return Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete profile image: " + e.getMessage());
        }
    }

    @Override
    public UserProfile updateProfileImage(UserProfile profile) {
        return profileRepository.save(profile);
    }

    @Override
    public UserProfileResponse removeImage(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        UserProfile profile = profileRepository.findByUserId(user.getId())
                .orElse(UserProfile.builder().user(user).build());
        profile.setProfileImage(null);

        return userProfileMapper.toResponse(profileRepository.save(profile));
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private boolean isValidImageType(String contentType) {
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/webp")
        );
    }
}