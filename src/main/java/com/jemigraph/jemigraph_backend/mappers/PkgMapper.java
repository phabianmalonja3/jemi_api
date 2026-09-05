package com.jemigraph.jemigraph_backend.mappers;

import com.jemigraph.jemigraph_backend.DTO.PkgDTO;
import com.jemigraph.jemigraph_backend.Entities.Pkg;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PkgMapper {

    @Mapping(source = "id" ,target = "id")
    PkgDTO toDto(Pkg entity);
    Pkg toEntity(PkgDTO dto);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(PkgDTO dto, @MappingTarget Pkg entity);
}