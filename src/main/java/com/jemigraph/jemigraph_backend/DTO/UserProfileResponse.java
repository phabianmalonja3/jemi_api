package com.jemigraph.jemigraph_backend.DTO;

import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String name,
        String email,
        String displayName,
        String phone,
        String location,
        String bio,
        String profileImage,
        String role,
        boolean isOnline,
         boolean isVerified,
        String instagram,
        String facebook,
        String twitter,
        String linkedin,
        String website,
        Double averageRating ,
        Long totalReviews

) {}