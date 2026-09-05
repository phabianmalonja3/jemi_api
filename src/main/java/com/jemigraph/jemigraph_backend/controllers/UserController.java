package com.jemigraph.jemigraph_backend.controllers;

import com.jemigraph.jemigraph_backend.DTO.ApiResponseDTO;
import com.jemigraph.jemigraph_backend.DTO.UserDTO;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;  // ✅ Add this

    @PostMapping("/profile-image")
    public ResponseEntity<?> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Please select a file to upload")
            );
        }

        String userEmail = authentication.getName();

        try {
            String fileName = userService.saveProfileImage(file, userEmail);
            return ResponseEntity.ok(
                    Map.of(
                            "message", "Image uploaded successfully",
                            "imageUrl", "/uploads/" + fileName
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("message", "Failed to upload image: " + e.getMessage())
            );
        }
    }

    // ============================================================
    // ✅ FCM TOKEN ENDPOINTS
    // ============================================================

    // ✅ Update FCM Token - PUT method
    @PostMapping("/fcm-token")
    public ResponseEntity<ApiResponseDTO> updateFcmToken(
            Authentication authentication,
            @RequestBody Map<String, String> request) {

        try {
            String email = authentication.getName();
            log.info("📱 Updating FCM token for user: {}", email);

            String fcmToken = request.get("fcmToken");
            if (fcmToken == null || fcmToken.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponseDTO.builder()
                                .success(false)
                                .message("FCM token is required")
                                .build());
            }

            // Find user
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update FCM token
            user.setFcmToken(fcmToken);
            userRepository.save(user);

            log.info("✅ FCM token updated successfully for user: {}", email);

            return ResponseEntity.ok(ApiResponseDTO.builder()
                    .success(true)
                    .message("FCM token updated successfully")
                    .data(Map.of("fcmToken", fcmToken))
                    .build());

        } catch (Exception e) {
            log.error("❌ Error updating FCM token: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDTO.builder()
                            .success(false)
                            .message("Failed to update FCM token: " + e.getMessage())
                            .build());
        }
    }

    // ✅ Update FCM Token - PATCH method (alternative)
    @PatchMapping("/fcm-token")
    public ResponseEntity<ApiResponseDTO> patchFcmToken(
            Authentication authentication,
            @RequestBody Map<String, String> request) {
        return updateFcmToken(authentication, request);
    }

    // ✅ Delete FCM Token (on logout)
    @DeleteMapping("/fcm-token")
    public ResponseEntity<ApiResponseDTO> deleteFcmToken(
            Authentication authentication) {

        try {
            String email = authentication.getName();
            log.info("🗑️ Deleting FCM token for user: {}", email);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Remove FCM token
            user.setFcmToken(null);
            userRepository.save(user);

            log.info("✅ FCM token deleted successfully for user: {}", email);

            return ResponseEntity.ok(ApiResponseDTO.builder()
                    .success(true)
                    .message("FCM token deleted successfully")
                    .build());

        } catch (Exception e) {
            log.error("❌ Error deleting FCM token: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponseDTO.builder()
                            .success(false)
                            .message("Failed to delete FCM token: " + e.getMessage())
                            .build());
        }
    }

    // ============================================================
    // EXISTING ENDPOINTS
    // ============================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        userService.removeUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable UUID id, @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateUser(id, userDTO));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable UUID id) {
        userService.updateStatus(id);
        return ResponseEntity.ok().build();
    }
}