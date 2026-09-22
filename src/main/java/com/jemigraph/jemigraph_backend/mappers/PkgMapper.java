package com.jemigraph.jemigraph_backend.mappers;

import com.jemigraph.jemigraph_backend.DTO.PhotographerPackageDTO;
import com.jemigraph.jemigraph_backend.Entities.Pkg;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PkgMapper {

  @Mapping(source = "id", target = "id")
  PhotographerPackageDTO toDto(Pkg entity);

  Pkg toEntity(PhotographerPackageDTO dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromDto(PhotographerPackageDTO dto, @MappingTarget Pkg entity);
}
