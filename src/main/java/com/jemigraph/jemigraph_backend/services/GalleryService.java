package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.Entities.Gallery;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.DTO.GalleryDTO;
import com.jemigraph.jemigraph_backend.enums.FileUploadType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface GalleryService {
    Gallery addMedia(User user, String fileUrl, FileUploadType fileType);
    List<GalleryDTO> getGalleryByUser(User user);
    void deleteMedia(UUID galleryId,  String userEMail);
}