package com.jemigraph.jemigraph_backend.controllers;

import com.jemigraph.jemigraph_backend.DTO.ProfileUpdateDTO;
import com.jemigraph.jemigraph_backend.DTO.UserProfileResponse;
import com.jemigraph.jemigraph_backend.Entities.UserProfile;
import com.jemigraph.jemigraph_backend.services.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserProfileController {


    private final UserProfileService userProfileService;

    @PutMapping
    public ResponseEntity<?> updateProfile(@Valid  @RequestBody ProfileUpdateDTO profileUpdateDTO, Principal principal) {
        String userEmail = principal.getName();
        UserProfile updatedProfile = userProfileService.updateProfile(userEmail, profileUpdateDTO);
        Map<String, Object> response = new HashMap<>();
        response.put("id", updatedProfile.getUser().getId());
        response.put("name", updatedProfile.getUser().getName());
        response.put("email", updatedProfile.getUser().getEmail());
        response.put("displayName", updatedProfile.getDisplayName());
        response.put("phone", updatedProfile.getPhone());
        response.put("location", updatedProfile.getLocation());
        response.put("bio", updatedProfile.getBio());
        response.put("instagram", updatedProfile.getInstagram());
        response.put("facebook", updatedProfile.getFacebook());
        response.put("twitter", updatedProfile.getTwitter());
        response.put("linkedin", updatedProfile.getLinkedin());
        response.put("website", updatedProfile.getWebsite());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getUserProfile(Principal principal) {
        UserProfileResponse profile = userProfileService.getProfileByEmail(principal.getName());

        return ResponseEntity.ok(profile);
    }

    @PostMapping("/profile-image")
    public ResponseEntity<?> uploadProfileImage(@RequestParam("file") MultipartFile file,

   Principal principal
    ) {

        String userEmail = principal.getName();
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        String imageUrl = userProfileService.saveProfileImage(userEmail,file);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    @DeleteMapping("/profile-image")
    public ResponseEntity<?> removeProfileImage(@AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = userDetails.getUsername();
        UserProfileResponse imageUrl = userProfileService.removeImage(userEmail);
        return ResponseEntity.ok().build();
    }
}