package com.jemigraph.jemigraph_backend.mappers;

import com.jemigraph.jemigraph_backend.DTO.PackageRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.jemigraph.jemigraph_backend.Entities.Pkg;

@Mapper(componentModel = "spring")
public interface PhotographyPackageMapper {


    // Kubadilisha Request DTO kwenda Entity (Wakati wa ku-save)
    Pkg toEntity(PackageRequestDto packageRequestDto);


    PackageRequestDto toDto(Pkg pkg);
}