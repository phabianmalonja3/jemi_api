package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.DTO.ProfileUpdateDTO;
import com.jemigraph.jemigraph_backend.DTO.UserProfileResponse;
import com.jemigraph.jemigraph_backend.Entities.UserProfile;
import org.springframework.web.multipart.MultipartFile;

public interface UserProfileService {

    UserProfile updateProfile(String userEmail, ProfileUpdateDTO dto);

    UserProfileResponse getProfileByEmail(String userEmail);

    String saveProfileImage(String email,MultipartFile file);

    boolean deleteProfileImage(String imageUrl);

    UserProfile updateProfileImage(UserProfile profile);  // Add this method

    UserProfileResponse removeImage(String userEmail);
}