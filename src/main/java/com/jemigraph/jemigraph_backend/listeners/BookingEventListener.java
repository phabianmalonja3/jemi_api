package com.jemigraph.jemigraph_backend.listeners;

import com.jemigraph.jemigraph_backend.events.BookingCreatedEvent;
import com.jemigraph.jemigraph_backend.services.FirebaseNotificationService;
import com.jemigraph.jemigraph_backend.services.NotificationService;
import com.jemigraph.jemigraph_backend.services.SmsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; // 👈 Tumia SLF4J sahihi
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class BookingEventListener {
    private final FirebaseNotificationService firebaseNotificationService;
    private final SmsService smsService;
private final NotificationService notificationService;
    private static final Logger log = LoggerFactory.getLogger(BookingEventListener.class);

    @Async
    @EventListener
    public void handleBookingCreated(BookingCreatedEvent event) {
        try {


            notificationService.createNotification(
                    event.photographerId(),
                    "New Booking Request!",
                    "You have received a new booking request from " + event.clientName() + ". Tap to review.",
                    event.bookingId()
            );
            if (event.photographerFcmToken() != null && !event.photographerFcmToken().isBlank()) {
                firebaseNotificationService.sendPushNotification(
                        event.photographerFcmToken(),
                        "New Booking Request!",
                        "You have received a new booking request from " + event.clientName() + ". Tap to review.",
                        Map.of("bookingId", event.bookingId().toString(),
                                "senderAvatar", event.clientAvatar() != null ? event.clientAvatar() : "",
                                "type", "NEW_BOOKING_SCREEN"),
                        "photographer",
                        ""
                );
            }


            if (event.photographerPhone() != null && !event.photographerPhone().isBlank()) {
                String smsMessage = "You have a new booking request from " + event.clientName()
                        + " for " + event.formattedPickupTime()
                        +" At " + event.addressName()
                        +" With Type "+event.bookingType()
                        + " awaiting your approval.";
                smsService.sendSms(event.photographerPhone(), smsMessage);
            }
        } catch (Exception e) {
            log.error("Failed to process async notifications for booking {}: {}", event.bookingId(), e.getMessage());
        }
    }
}