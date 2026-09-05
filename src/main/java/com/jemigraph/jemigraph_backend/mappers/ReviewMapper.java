package com.jemigraph.jemigraph_backend.mappers;

import com.jemigraph.jemigraph_backend.DTO.ReviewResponseDTO;
import com.jemigraph.jemigraph_backend.events.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    @Mapping(source = "client.name", target = "clientName")
    @Mapping(source = "photographer.name", target = "photographerName")
    @Mapping(source = "photographer.averageRating", target = "photographerAverageRating")
    @Mapping(source = "photographer.totalReviews", target = "photographerTotalReviews")
    ReviewResponseDTO toResponseDTO(Review review);
    List<ReviewResponseDTO> toResponseDTOList(List<Review> reviews);
}