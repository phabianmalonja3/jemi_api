package com.jemigraph.jemigraph_backend.DTO;


import com.jemigraph.jemigraph_backend.Entities.Gallery;
import com.jemigraph.jemigraph_backend.Entities.Location;
import com.jemigraph.jemigraph_backend.Entities.Pkg;
import com.jemigraph.jemigraph_backend.enums.UserRole;
import lombok.Data;

import java.util.List;
import java.util.UUID;
@Data
public class PhotographerProfileDTO {
    private UUID id;
    private String name;
    private String email;
    private UserRole role;
    private Double rating;
    private List<PkgDTO> packages;
    private Boolean isOnline;
    private Boolean isBusy;
    private List<GalleryDTO> gallery;
    private Boolean isVerified;
    private String profileImageUrl;
    private String displayName;

    private double  latitude;
    private double longitude;
    private  String address;
    private String phone;
    private String bio;
    private String profileImage;
    private String instagram;
    private String facebook;
    private String twitter;
    private String linkedin;
    private String website;
    private Double averageRating;
    private Long totalReviews;

}