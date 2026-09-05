package com.jemigraph.jemigraph_backend.listeners;

import com.jemigraph.jemigraph_backend.events.PhotographerVerifiedEvent;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.services.EmailService;
import com.jemigraph.jemigraph_backend.services.FirebaseNotificationService;
import com.jemigraph.jemigraph_backend.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PhotographerVerifiedListener {

    private final UserRepository userRepository;
    private final FirebaseNotificationService firebaseNotificationService;
    private final NotificationService notificationService;

    private final EmailService emailService;


    @Async("notificationExecutor")
    @EventListener
    public void handlePhotographerVerifiedEvent(PhotographerVerifiedEvent event) {
        String title = "Account Verified!";
        String body = "Congratulations! Your account has been verified. You can now start receiving bookings.";
        var dbNotification = notificationService.createNotification(
                event.user().getId(), title, body, null);
        String token = userRepository.findFcmTokenByUserId(event.user().getId());
        if (token == null || token.isBlank()) return;
        Map<String, String> data = new HashMap<>();
        data.put("type", "VERIFICATION_SUCCESS");
        data.put("notificationId", dbNotification.getId().toString());
        try {
            firebaseNotificationService.sendPushNotification(token, title, body, data,"photographer","");


            log.info("✅ Verification notification sent to photographer: {}", event.user().getId());

            emailService.sendVerification(event);
        } catch (Exception e) {
            log.error("❌ Firebase error for verification notification: {}", e.getMessage());
        }
    }

}