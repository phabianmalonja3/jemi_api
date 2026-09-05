package com.jemigraph.jemigraph_backend.repositories;

import com.jemigraph.jemigraph_backend.Entities.Gallery;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.enums.FileUploadType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GalleryRepository extends JpaRepository<Gallery, UUID> {
    List<Gallery> findByUser(User user);
    long countByUserAndFileType(User user, FileUploadType fileType);
}