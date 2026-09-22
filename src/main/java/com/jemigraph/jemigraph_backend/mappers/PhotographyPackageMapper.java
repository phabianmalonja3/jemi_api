package com.jemigraph.jemigraph_backend.mappers;

import com.jemigraph.jemigraph_backend.DTO.PhotographerPackageRequestDto;
import com.jemigraph.jemigraph_backend.Entities.Pkg;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PhotographyPackageMapper {

  Pkg toEntity(PhotographerPackageRequestDto photographerPackageRequestDto);

  PhotographerPackageRequestDto toDto(Pkg pkg);
}
