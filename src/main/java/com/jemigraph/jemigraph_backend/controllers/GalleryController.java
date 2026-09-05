package com.jemigraph.jemigraph_backend.controllers;

import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.DTO.GalleryDTO;
import com.jemigraph.jemigraph_backend.enums.FileUploadType;
import com.jemigraph.jemigraph_backend.mappers.GaleryMapper;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.services.GalleryService;
import com.jemigraph.jemigraph_backend.services.impl.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/gallery")
@RequiredArgsConstructor
public class GalleryController {
    private final FileStorageService fileStorageService;
    private final GalleryService galleryService;
    private final UserRepository userRepository;
    private final GaleryMapper galeryMapper;
    @PostMapping("/upload")
    public ResponseEntity<?> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String fileType,
            Principal principal) {
        try {
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            String fileUrl = fileStorageService.storeFile(file);
            return ResponseEntity.ok(galeryMapper.toDto(galleryService.addMedia(user, fileUrl, FileUploadType.valueOf(fileType))));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error existing: " + e.getMessage());
        }
    }


    @GetMapping
    public ResponseEntity<List<GalleryDTO>> getMyGallery(Principal  principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(()->new RuntimeException("Not Found"));
        return ResponseEntity.ok(galleryService.getGalleryByUser(user));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMedia(
            @PathVariable UUID id,
            Principal principal) {

        try {
            galleryService.deleteMedia(id, principal.getName());
            return ResponseEntity.ok("Media Deleted!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}