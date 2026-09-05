package com.jemigraph.jemigraph_backend.mappers;

import com.jemigraph.jemigraph_backend.DTO.GalleryDTO;
import com.jemigraph.jemigraph_backend.Entities.Gallery;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GaleryMapper {
    GalleryDTO toDto(Gallery gallery);
    Gallery toEntity(GalleryDTO galleryDTO);
}
