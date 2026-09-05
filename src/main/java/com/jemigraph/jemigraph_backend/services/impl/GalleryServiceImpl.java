package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.Entities.Gallery;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.DTO.GalleryDTO;
import com.jemigraph.jemigraph_backend.enums.FileUploadType;
import com.jemigraph.jemigraph_backend.repositories.GalleryRepository;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.services.GalleryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GalleryServiceImpl implements GalleryService {

    private final GalleryRepository galleryRepository;
    private final UserRepository userRepository;
    @Override
    @Transactional
    public Gallery addMedia(User user, String fileUrl, FileUploadType fileType) {
        // Kwanza, piga mahesabu
        long currentCount = galleryRepository.countByUserAndFileType(user, fileType);

        if (fileType == FileUploadType.IMAGE && currentCount >= 5) {
            throw new IllegalStateException("You have reached the limit of 5 images.");
        }


        if (fileType == FileUploadType.VIDEO && currentCount >= 1) {
            throw new IllegalStateException("You can only upload one video.");
        }

        Gallery gallery = new Gallery();
        gallery.setUser(user);
        gallery.setFileUrl(fileUrl);
        gallery.setFileType(fileType);

        return galleryRepository.save(gallery);
    }

    @Override
    public List<GalleryDTO> getGalleryByUser(User user) {
        return galleryRepository.findByUser(user).stream()
                .map(g -> {
                    GalleryDTO dto = new GalleryDTO();
                    dto.setId(g.getId());
                    dto.setFileUrl(g.getFileUrl());
                    dto.setFileType(g.getFileType().toString());
                    return dto;
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteMedia(UUID galleryId,  String email) {

        User user =  userRepository.findByEmail(
                email
        ).orElseThrow(()-> new RuntimeException("User doest not allowed"));
        Gallery gallery = galleryRepository.findById(galleryId)
                .orElseThrow(() -> new RuntimeException("Media Does't exist"));
        if (!gallery.getUser().getId().equals(user.getId())) {
            throw new SecurityException("No role to upload this files.");
        }

        galleryRepository.delete(gallery);
    }
}