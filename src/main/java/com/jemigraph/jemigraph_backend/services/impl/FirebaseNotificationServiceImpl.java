
package com.jemigraph.jemigraph_backend.services.impl;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.jemigraph.jemigraph_backend.services.FirebaseNotificationService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FirebaseNotificationServiceImpl implements FirebaseNotificationService {

    @Override
    public void sendPushNotification(
            String fcmToken,
            String title,
            String body,
            Map<String, String> dataPayload,
            String appName,
            String imageUrl
    ) {

        // 1. Kinga ya usalama: Hakikisha token ipo
        if (fcmToken == null || fcmToken.trim().isEmpty()) {
            System.out.println(
                    "⚠️ [FCM] Kifaa hakina FCM Token halali kwenye app ya ["
                            + appName
                            + "]. Notification imesitishwa."
            );
            return;
        }

        try {

            com.google.firebase.FirebaseApp app =
                    com.google.firebase.FirebaseApp.getInstance(appName);

            com.google.firebase.messaging.FirebaseMessaging messaging =
                    com.google.firebase.messaging.FirebaseMessaging.getInstance(app);

            // 3. Hakikisha imageUrl inaingia kwenye dataPayload ili Flutter iweze kuitumia kusoma picha
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                dataPayload.put("imageUrl", imageUrl);
                System.out.println("🖼️ [FCM] Notification image imeongezwa kwenye data payload: " + imageUrl);
            }

            // 4. Tengeneza Notification Builder (Kwa ajili ya Title na Body pekee)
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // 5. Tengeneza Message na kuunganisha dataPayload (pamoja na click_action na imageUrl)
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(notification)
                    .putAllData(dataPayload)
                    .build();

            // 6. Tuma notification Firebase
            String response = messaging.send(message);

            System.out.println(
                    "🚀 [FCM] Push Notification imetumwa kikamilifu kupitia ["
                            + appName
                            + "]! Firebase Message ID: "
                            + response
            );

        } catch (com.google.firebase.messaging.FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() ==
                    com.google.firebase.messaging.MessagingErrorCode.UNREGISTERED
                    || e.getMessagingErrorCode() ==
                    com.google.firebase.messaging.MessagingErrorCode.INVALID_ARGUMENT) {

                System.err.println(
                        "⚠️ [FCM] Token imepitwa na wakati au si sahihi kwenye app ya ["
                                + appName
                                + "]: "
                                + fcmToken
                );
            } else {
                System.err.println(
                        "❌ [FCM] Hitilafu ya Firebase kwenye app ya ["
                                + appName
                                + "]: "
                                + e.getMessage()
                );
            }
        } catch (Exception e) {
            System.err.println(
                    "❌ [FCM] Hitilafu imetokea wakati wa kurusha notification: "
                            + e.getMessage()
            );
            e.printStackTrace();
        }
    }


}
