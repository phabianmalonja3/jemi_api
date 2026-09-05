package com.jemigraph.jemigraph_backend.mappers;


import com.jemigraph.jemigraph_backend.DTO.UserDTO;
import com.jemigraph.jemigraph_backend.Entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDto(User user);

    @Mapping(target = "id",source = "id" )
    User toEntity(UserDTO userDto);


    List<UserDTO> toDtoList(List<User> users);
}
