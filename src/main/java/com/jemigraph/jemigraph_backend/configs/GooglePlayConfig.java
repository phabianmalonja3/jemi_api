package com.jemigraph.jemigraph_backend.configs;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class GooglePlayConfig {

  private static final String APPLICATION_NAME = "MySystemDownloadTracker";
  private static final String SERVICE_ACCOUNT_KEY_PATH = "src/main/resources/service-account.json";

  public static AndroidPublisher getAndroidPublisherService()
      throws IOException, GeneralSecurityException {

    GoogleCredentials credentials =
        GoogleCredentials.fromStream(new FileInputStream(SERVICE_ACCOUNT_KEY_PATH))
            .createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));
    return new AndroidPublisher.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            new HttpCredentialsAdapter(credentials))
        .setApplicationName(APPLICATION_NAME)
        .build();
  }
}
