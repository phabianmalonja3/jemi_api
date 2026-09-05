package com.jemigraph.jemigraph_backend.mappers;

import com.jemigraph.jemigraph_backend.DTO.TeamCreateDTO;
import com.jemigraph.jemigraph_backend.DTO.TeamResponseDTO;
import com.jemigraph.jemigraph_backend.Entities.Team;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TeamMapper {
    TeamResponseDTO toResponseDto(Team team);
    Team toEntity(TeamCreateDTO createDto);

}