package com.jemigraph.jemigraph_backend.controllers;

import com.jemigraph.jemigraph_backend.Entities.Notification;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(Principal principal) {
        List<Notification> unread = notificationService.getUnreadNotifications(principal.getName());
        return ResponseEntity.ok(unread);
    }
    @GetMapping("/all")
    public ResponseEntity<List<Notification>> getAllNotifications(Principal principal) {
        String userEmail = principal.getName();
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        List<Notification> allNotifications = notificationService.getAllUserNotifications(user.getId());
        return ResponseEntity.ok(allNotifications);
    }
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable UUID notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/{notificationId}/read")
    public ResponseEntity<Void> removeNotification(@PathVariable UUID notificationId,
    Principal principle
    ) {
        notificationService.removeNotification(notificationId,principle.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/mark-all-read")
    public ResponseEntity<Void> markAllNotificationsAsRead(@RequestParam UUID userId) {
        notificationService.markAllUserNotificationsAsRead(userId);
        return ResponseEntity.noContent().build();
    }
}