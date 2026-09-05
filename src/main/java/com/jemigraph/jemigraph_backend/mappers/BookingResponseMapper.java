package com.jemigraph.jemigraph_backend.mappers;

import com.jemigraph.jemigraph_backend.DTO.BookingResponseDTO;
import com.jemigraph.jemigraph_backend.DTO.PackageSummaryDTO;
import com.jemigraph.jemigraph_backend.DTO.ReviewSummaryDTO;
import com.jemigraph.jemigraph_backend.DTO.UserSummaryDTO;
import com.jemigraph.jemigraph_backend.Entities.Bookings;
import com.jemigraph.jemigraph_backend.Entities.Pkg;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.events.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BookingResponseMapper {

    BookingResponseMapper INSTANCE = Mappers.getMapper(BookingResponseMapper.class);

    @Mapping(source = "pkg.price", target = "amountPaid")
    BookingResponseDTO toResponseDTO(Bookings booking);

    // Optional nested mappings (if field names differ)
    UserSummaryDTO toUserSummaryDTO(User user);
    PackageSummaryDTO toPackageSummaryDTO(Pkg pkg);
    ReviewSummaryDTO toReviewSummaryDTO(Review review);
}