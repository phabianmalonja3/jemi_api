package com.jemigraph.jemigraph_backend.mappers;

import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.DTO.PhotographerProfileDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface PhotographerMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "role", target = "role")
    @Mapping(source = "verified", target = "isVerified")
    @Mapping(source = "isOnline", target = "isOnline")

    // Rating mappings
    @Mapping(source = "averageRating", target = "averageRating")
    @Mapping(source = "averageRating", target = "rating")
    @Mapping(source = "totalReviews", target = "totalReviews")

    // Taarifa za UserProfile
    @Mapping(source = "userProfile.displayName", target = "displayName")
    @Mapping(source = "userProfile.phone", target = "phone")
    @Mapping(source = "userProfile.bio", target = "bio")
    @Mapping(source = "userProfile.profileImage", target = "profileImage")
    @Mapping(source = "userProfile.profileImage", target = "profileImageUrl")
    @Mapping(source = "userProfile.instagram", target = "instagram")
    @Mapping(source = "userProfile.facebook", target = "facebook")
    @Mapping(source = "userProfile.twitter", target = "twitter")
    @Mapping(source = "userProfile.linkedin", target = "linkedin")
    @Mapping(source = "userProfile.website", target = "website")

    // 🔥 IMESAHIHISHWA: Location inatoka moja kwa moja kwenye User Entity (sio userProfile)
    // Angalia kama uwanja wa anwani kwenye Location entity unaitwa address, addressName, au name
    @Mapping(source = "location.address", target = "address")
    @Mapping(source = "location.latitude", target = "latitude")
    @Mapping(source = "location.longitude", target = "longitude")

    PhotographerProfileDTO toDto(User user);

    @Mapping(source = "averageRating", target = "averageRating")
    @Mapping(source = "totalReviews", target = "totalReviews")
    @Mapping(target = "userProfile.displayName", source = "displayName")
    @Mapping(target = "userProfile.phone", source = "phone")
    @Mapping(target = "userProfile.bio", source = "bio")
    @Mapping(target = "userProfile.profileImage", source = "profileImage")
    @Mapping(target = "userProfile.instagram", source = "instagram")
    @Mapping(target = "userProfile.facebook", source = "facebook")
    @Mapping(target = "userProfile.twitter", source = "twitter")
    @Mapping(target = "userProfile.linkedin", source = "linkedin")
    @Mapping(target = "userProfile.website", source = "website")
    User toEntity(PhotographerProfileDTO userDto);
}