package com.jemigraph.jemigraph_backend.DTO;

import com.jemigraph.jemigraph_backend.Entities.Location;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

@Data
@Builder
public class UserResponseDto {
    private String id;
    private String name;
    private String email;
    private String role;
    private String profileImageUrl;
    private boolean isOnline;
    private boolean isBusy;
    private String subscriptionStatus;
    private LocalDateTime trialEndsAt;
    private boolean verified;
    Location location;
    private double averageRating;
    private int totalReviews;
    private String deviceType;
    private LocalDateTime createdAt;
}