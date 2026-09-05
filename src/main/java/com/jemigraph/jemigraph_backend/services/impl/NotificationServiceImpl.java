package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.Entities.Notification;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.repositories.NotificationRepository;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Notification createNotification(UUID recipientId, String title, String body, UUID bookingId) {
        Notification notification = new Notification();
        notification.setUserId(recipientId);
        notification.setTitle(title);
        notification.setMessage(body);
        notification.setBookingId(bookingId);
        notification.setIsRead(false); // Default ni haijasomwa
        notification.setCreatedAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getUnreadNotifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId());
    }

    @Override
    public List<Notification> getAllUserNotifications(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
            System.out.println("✅ [Notification] Arifa " + notificationId + " imewekwa alama ya IS_READ");
        });
    }

    @Override
    @Transactional
    public void markAllUserNotificationsAsRead(UUID userId) {
        notificationRepository.markAllAsRead(userId);
        System.out.println("✅ [Notification] Arifa zote za user " + userId + " zimewekwa alama ya IS_READ");
    }

    @Override
    public void removeNotification(UUID notificationId, String email) {
      notificationRepository.delete(notificationRepository.findById(notificationId).orElseThrow(()-> new RuntimeException("not found!"))
       );

    }
}