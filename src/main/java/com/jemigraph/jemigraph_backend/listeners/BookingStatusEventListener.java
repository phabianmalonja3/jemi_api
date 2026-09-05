package com.jemigraph.jemigraph_backend.listeners;

import com.jemigraph.jemigraph_backend.enums.BookingStatus;
import com.jemigraph.jemigraph_backend.events.BookingStatusChangedEvent;
import com.jemigraph.jemigraph_backend.services.FirebaseNotificationService;
import com.jemigraph.jemigraph_backend.services.NotificationService;
import com.jemigraph.jemigraph_backend.services.SmsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class BookingStatusEventListener {

    private final FirebaseNotificationService firebaseNotificationService;
    private final SmsService smsService;
    private final NotificationService notificationService;

    private static final Logger log = LoggerFactory.getLogger(BookingStatusEventListener.class);

    @Async
    @EventListener
    public void handleBookingStatusChanged(BookingStatusChangedEvent event) {
        try {
            String title = "";
            String body = "";
            String smsMessage = "";
            String type = "";

            String pName = (event.photographerName() != null && !event.photographerName().isBlank())
                    ? event.photographerName()
                    : "The photographer";

            switch (event.newStatus()) {
                case ACCEPTED -> {
                    title = "Booking Accepted!";
                    body = pName + " has accepted your request and is preparing.";
                    smsMessage = "Congratulations! Your Jemigraph booking has been accepted by " + pName + ".";
                    type = "BOOKING_ACCEPTED";
                }
                case CANCELLED -> {
                    title = "Booking Rejected";
                    body = "Unfortunately, " + pName + " had to decline your booking request.";
                    smsMessage = "Your Jemigraph booking request was declined by " + pName + ".";
                    type = "BOOKING_REJECTED";
                }
                case EN_ROUTE -> {
                    title = "Photographer is on the way!";
                    body = pName + " is heading to your location. Tap to view on map.";
                    smsMessage = "Your Jemigraph photographer (" + pName + ") is on the way to your location!";
                    type = "EN_ROUTE";
                }
                case COMPLETED -> {
                    title = "Session Completed!";
                    body = "Your photo session with " + pName + " has been marked as completed. Thank you!";
                    smsMessage = "Your Jemigraph photo session with " + pName + " is completed. Thank you for choosing us!";
                    type = "BOOKING_COMPLETED";
                }
                default -> {
                    return;
                }
            }

            // Hapa sasa title na body zimejazwa vizuri kabla ya kuhifadhi kwenye database
            notificationService.createNotification(
                    event.clientId(),
                    title,
                    body,
                    event.bookingId()
            );

            if (event.clientFcmToken() != null && !event.clientFcmToken().isEmpty()) {
                // Hakikisha event yako ina getter ya photographerProfileImageUrl (Badilisha kama jina linatofautiana)
                String profileImageUrl = event.profileImage() != null
                        ? event.profileImage()
                        : "";

                firebaseNotificationService.sendPushNotification(
                        event.clientFcmToken(),
                        title,
                        body,
                        Map.of(
                                "bookingId", event.bookingId().toString(),
                                "type", type,
                                "profileImageUrl", profileImageUrl
                        ),
                        "client",
                        ""
                );
            }

            if (event.clientPhone() != null && !event.clientPhone().isBlank()) {
                smsService.sendSms(event.clientPhone(), smsMessage);
            }

        } catch (Exception e) {
            log.error("Failed to process async status change notifications for booking {}: {}", event.bookingId(), e.getMessage());
        }
    }
}