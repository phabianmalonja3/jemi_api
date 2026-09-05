package com.jemigraph.jemigraph_backend.mappers;

import com.jemigraph.jemigraph_backend.DTO.BookingDTO;
import com.jemigraph.jemigraph_backend.Entities.Bookings;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(target = "photographerId", source = "photographer.id")
    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "packageId", source = "pkg.id")
    @Mapping(target = "amountPaid", source = "pkg.price")
    @Mapping(target = "photographerName", source = "photographer.name")
    @Mapping(target = "clientName", source = "client.name")
    @Mapping(target = "packageName", source = "pkg.name")
    BookingDTO toDto(Bookings entity);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "photographer", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "pkg", ignore = true)
    Bookings toEntity(BookingDTO dto);
}