package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.Entities.Notification;
import java.util.List;
import java.util.UUID;

public interface NotificationService {
    Notification createNotification(UUID userId, String title, String message, UUID bookingId);
    List<Notification> getUnreadNotifications(String email);
    List<Notification> getAllUserNotifications(UUID userId);
    void markAsRead(UUID notificationId);

    void markAllUserNotificationsAsRead(UUID userId);

    void removeNotification(UUID notificationId, String name);
}