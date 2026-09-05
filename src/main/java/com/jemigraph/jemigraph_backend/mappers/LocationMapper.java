package com.jemigraph.jemigraph_backend.mappers;


import com.jemigraph.jemigraph_backend.DTO.LocationResponseDTO;
import com.jemigraph.jemigraph_backend.Entities.Location;
import com.jemigraph.jemigraph_backend.Entities.LocationRequestDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationRequestDTO toDto(Location location);
    LocationResponseDTO toDtoResponse(Location location);
    Location toEntity(LocationRequestDTO locationRequestDTO);

}
