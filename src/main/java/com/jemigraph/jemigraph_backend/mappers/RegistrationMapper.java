package com.jemigraph.jemigraph_backend.mappers;

import com.jemigraph.jemigraph_backend.DTO.RegistrationResponseDTO;
import com.jemigraph.jemigraph_backend.Entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring", imports = {LocalDateTime.class})
public interface RegistrationMapper {
    @Mapping(source = "createdAt", target = "registeredAt")
    @Mapping(source = "verified", target = "isVerified")
    @Mapping(target = "trialEndsAt", expression = "java(user.getRole().name().equals(\"PHOTOGRAPHER\") ? LocalDateTime.now().plusMonths(1) : null)")
    RegistrationResponseDTO toDto(User user);
}