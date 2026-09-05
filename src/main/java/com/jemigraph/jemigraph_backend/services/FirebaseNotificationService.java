package com.jemigraph.jemigraph_backend.services;

import java.util.Map;


import java.util.Map;

public interface FirebaseNotificationService {

    /**
     * Tuma push notification kwa mtumiaji kupitia Token ya Firebase (FCM Token)
     *
     * @param fcmToken     Token ya kifaa cha mtumiaji
     * @param title        Kichwa cha habari cha notification
     * @param body         Ujumbe wenyewe
     * @param dataPayload  Data za ziada (Metadata)
     * @param appName      Jina la Firebase App ("client" au "photographer")
     * @param imageUrl     URL ya picha ya notification (optional, inaweza kuwa null)
     */
    void sendPushNotification(
            String fcmToken,
            String title,
            String body,
            Map<String, String> dataPayload,
            String appName,
            String imageUrl
    );
}

