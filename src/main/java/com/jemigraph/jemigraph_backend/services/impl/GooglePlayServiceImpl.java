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
import java.io.FileInputStream;
import java.util.Collections;
import org.springframework.stereotype.Service;

@Service
public class GooglePlayServiceImpl implements GooglePlayService {

  private static final String APPLICATION_NAME = "MySystemDownloadTracker";
  private static final String SERVICE_ACCOUNT_KEY_PATH =
      "/opt/jemigraph/secrets/service-account.json";

  //	private static final String SERVICE_ACCOUNT_KEY_PATH =
  // "src/main/resources/service-account.json";

  @Override
  public AndroidPublisher getAndroidPublisherService() throws Exception {
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

    // Anzisha App Edit session ili kupata taarifa za tracks (Production, Beta, n.k.)
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

    // Anzisha App Edit session ili kusoma App Bundles zilizopakiwa
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
        + ". (Kumbuka: Kwa takwimu za kina za idadi ya vipakuliwa vya watumiaji, hutolewa kupitia Google Cloud Storage Reports).";
  }
}
