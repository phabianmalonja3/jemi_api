package com.jemigraph.jemigraph_backend.services.impl;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.AppEdit;
import com.google.api.services.androidpublisher.model.BundlesListResponse;
import com.google.api.services.androidpublisher.model.TracksListResponse;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.jemigraph.jemigraph_backend.services.GooglePlayService;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Service
public class GooglePlayServiceImpl implements GooglePlayService {
  private static final String APPLICATION_NAME = "MySystemDownloadTracker";

  private final String serviceAccountKeyPath;
  private final ResourceLoader resourceLoader;

  public GooglePlayServiceImpl(
      @Value("${app.google.play-key-path}") String serviceAccountKeyPath,
      ResourceLoader resourceLoader) {
    this.serviceAccountKeyPath = serviceAccountKeyPath;
    this.resourceLoader = resourceLoader;
  }

  @Override
  public AndroidPublisher getAndroidPublisherService() throws Exception {
    Resource resource = resourceLoader.getResource(serviceAccountKeyPath);

    GoogleCredentials credentials =
        GoogleCredentials.fromStream(resource.getInputStream())
            .createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));

    return new AndroidPublisher.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            new HttpCredentialsAdapter(credentials))
        .setApplicationName(APPLICATION_NAME)
        .build();
  }

  @Override
  public String checkAppDetails(String packageName) {
    try {
      AndroidPublisher publisher = getAndroidPublisherService();
      return "Umeunganishwa kwa mafanikio na App: " + packageName;
    } catch (Exception e) {
      return "Imeshindikana kuunganisha: " + e.getMessage();
    }
  }

  @Override
  public String fetchAppListingDetails(String packageName) throws Exception {
    AndroidPublisher publisher = getAndroidPublisherService();

    AndroidPublisher.Edits edits = publisher.edits();
    AppEdit editRequest = edits.insert(packageName, null).execute();
    String editId = editRequest.getId();

    TracksListResponse tracksResponse = edits.tracks().list(packageName, editId).execute();

    int tracksCount = (tracksResponse.getTracks() != null) ? tracksResponse.getTracks().size() : 0;
    return "Mafanikio! App ina jumla ya Tracks (njia za usambazaji) "
        + tracksCount
        + " zilizosanidiwa.";
  }

  @Override
  public String fetchDownloadStatistics(String packageName) throws Exception {
    AndroidPublisher publisher = getAndroidPublisherService();

    AndroidPublisher.Edits edits = publisher.edits();
    AppEdit editRequest = edits.insert(packageName, null).execute();
    String editId = editRequest.getId();

    BundlesListResponse bundlesResponse = edits.bundles().list(packageName, editId).execute();
    int bundleCount =
        (bundlesResponse.getBundles() != null) ? bundlesResponse.getBundles().size() : 0;

    return "Takwimu za Mfumo ("
        + packageName
        + "): Jumla ya App Bundles zilizopo kwenye Console ni "
        + bundleCount
        + ".";
  }
}
