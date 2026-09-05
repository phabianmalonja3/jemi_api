package com.jemigraph.jemigraph_backend.listeners;

import com.jemigraph.jemigraph_backend.enums.PaymentStatus;
import com.jemigraph.jemigraph_backend.events.PaymentEvent;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.services.FirebaseNotificationService;
import com.jemigraph.jemigraph_backend.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentNotificationListener {

    private final UserRepository userRepository;
    private final FirebaseNotificationService firebaseNotificationService;
    private final NotificationService notificationService;

    @Async("notificationExecutor")
    @EventListener
    public void handlePaymentEvent(PaymentEvent event) {

        String title = (event.status() == PaymentStatus.FULLY_PAID)
                ? "Payment Complete! "
                : "Initial Payment Received ";

        String body = (event.status() == PaymentStatus.FULLY_PAID)
                ? "Great news! You have received the final 60% payment for your booking."
                : "Your client has paid the 40% deposit (Tsh" + event.amount() + "). You can now prepare for the session.";

        UUID savedNotificationId = null;
        try {
            var dbNotification = notificationService.createNotification(
                    event.photographerId(), title, body, event.bookingId());
            savedNotificationId = dbNotification.getId();
        } catch (Exception e) {
            log.error("❌ Imeshindikana kuhifadhi payment notification: {}", e.getMessage());
        }


        String token = userRepository.findFcmTokenByUserId(event.photographerId());
        if (token == null || token.isBlank()) return;

        Map<String, String> data = new HashMap<>();
        data.put("bookingId", event.bookingId().toString());
        data.put("type", "PAYMENT_UPDATE");
        if (savedNotificationId != null) data.put("notificationId", savedNotificationId.toString());

        try {
            firebaseNotificationService.sendPushNotification(token, title, body, data,"client","");
            log.info("🚀 Payment notification sent for booking: {}", event.bookingId());
        } catch (Exception e) {
            log.error("❌ Firebase error: {}", e.getMessage());
        }
    }
}