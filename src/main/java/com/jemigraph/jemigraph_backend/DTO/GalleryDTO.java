package com.jemigraph.jemigraph_backend.DTO;

import lombok.Data;

import java.util.UUID;

@Data
public class GalleryDTO {
    private UUID id;
    private String fileUrl;
    private String fileType;
}
