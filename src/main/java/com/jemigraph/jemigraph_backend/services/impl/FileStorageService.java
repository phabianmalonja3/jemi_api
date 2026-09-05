package com.jemigraph.jemigraph_backend.services.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class    FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String storeFile(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir, "media")
                    .toAbsolutePath()
                    .normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = UUID.randomUUID()
                    + "_"
                    + file.getOriginalFilename();
            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(
                    file.getInputStream(),
                    targetLocation,
                    StandardCopyOption.REPLACE_EXISTING
            );
            return "/uploads/media/" + fileName;
        } catch (IOException ex) {
            throw new RuntimeException(
                    "Failed to upload !: " + file.getOriginalFilename(),
                    ex
            );
        }
    }

    public String storeProfile(MultipartFile file) {
        try {

            Path uploadPath = Paths.get(uploadDir, "profiles")
                    .toAbsolutePath()
                    .normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = UUID.randomUUID()
                    + "_"
                    + file.getOriginalFilename();

            Path targetLocation = uploadPath.resolve(fileName);

            Files.copy(
                    file.getInputStream(),
                    targetLocation,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return "/uploads/profiles/" + fileName;

        } catch (IOException ex) {
            throw new RuntimeException(
                    "Failed to upload !: "  + file.getOriginalFilename(),
                    ex
            );
        }
    }

    public String storeTeamImage(MultipartFile file) {
        try {
            // Define directory specifically for team images
            Path uploadPath = Paths.get(uploadDir, "teams")
                    .toAbsolutePath()
                    .normalize();

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate a unique filename to prevent overwriting conflicts
            String fileName = UUID.randomUUID()
                    + "_"
                    + file.getOriginalFilename();

            Path targetLocation = uploadPath.resolve(fileName);

            Files.copy(
                    file.getInputStream(),
                    targetLocation,
                    StandardCopyOption.REPLACE_EXISTING
            );

            // Return the relative database path for team assets
            return "/uploads/teams/" + fileName;

        } catch (IOException ex) {
            throw new RuntimeException(
                    "Failed to upload team image: " + file.getOriginalFilename(),
                    ex
            );
        }
    }

}

