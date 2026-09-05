package com.jemigraph.jemigraph_backend.mappers;


import com.jemigraph.jemigraph_backend.DTO.UserProfileResponse;
import com.jemigraph.jemigraph_backend.Entities.UserProfile;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {
    UserProfileMapper INSTANCE = Mappers.getMapper(UserProfileMapper.class);
    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "user.name", target = "name")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.verified", target = "isVerified")
    @Mapping(source = "user.role", target = "role")
    @Mapping(source = "user.isOnline", target = "isOnline")
    @Mapping(source = "user.averageRating", target = "averageRating")
    @Mapping(source = "user.totalReviews", target = "totalReviews")

    UserProfileResponse toResponse(UserProfile userProfile);
}
