package com.jemigraph.jemigraph_backend.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.AppEdit;
import com.google.api.services.androidpublisher.model.Bundle;
import com.google.api.services.androidpublisher.model.BundlesListResponse;
import com.google.api.services.androidpublisher.model.Track;
import com.google.api.services.androidpublisher.model.TrackRelease;
import com.google.api.services.androidpublisher.model.TracksListResponse;

import com.google.api.services.playdeveloperreporting.v1beta1.Playdeveloperreporting;
import com.google.api.services.playdeveloperreporting.v1beta1.PlaydeveloperreportingScopes;
import com.google.api.services.playdeveloperreporting.v1beta1.model.GooglePlayDeveloperReportingV1beta1ErrorReport;
import com.google.api.services.playdeveloperreporting.v1beta1.model.GooglePlayDeveloperReportingV1beta1SearchErrorReportsResponse;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.jemigraph.jemigraph_backend.services.GooglePlayService;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class GooglePlayServiceImpl implements GooglePlayService {

  private static final Logger log = LoggerFactory.getLogger(GooglePlayServiceImpl.class);
  private static final String APPLICATION_NAME = "MySystemDownloadTracker";

  private final Resource credentialsResource;
  private final ObjectMapper objectMapper = new ObjectMapper();

  private volatile AndroidPublisher publisher;
  private volatile Playdeveloperreporting reportingClient;

  public GooglePlayServiceImpl(@Value("${app.google.play-key-path}") Resource credentialsResource) {
    this.credentialsResource = credentialsResource;
  }

  // ---------------------------------------------------------------------------
  // Client construction (cached)
  // ---------------------------------------------------------------------------

  private AndroidPublisher publisher() throws IOException {
    AndroidPublisher p = this.publisher;
    if (p == null) {
      synchronized (this) {
        p = this.publisher;
        if (p == null) {
          try {
            GoogleCredentials credentials =
                loadCredentials(AndroidPublisherScopes.ANDROIDPUBLISHER);
            p =
                new AndroidPublisher.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        GsonFactory.getDefaultInstance(),
                        new HttpCredentialsAdapter(credentials))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            this.publisher = p;
          } catch (GeneralSecurityException e) {
            throw new IOException("Failed to build AndroidPublisher client", e);
          }
        }
      }
    }
    return p;
  }

  private Playdeveloperreporting reportingClient() throws IOException {
    Playdeveloperreporting r = this.reportingClient;
    if (r == null) {
      synchronized (this) {
        r = this.reportingClient;
        if (r == null) {
          try {
            GoogleCredentials credentials =
                loadCredentials(PlaydeveloperreportingScopes.PLAYDEVELOPERREPORTING);
            r =
                new Playdeveloperreporting.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        GsonFactory.getDefaultInstance(),
                        new HttpCredentialsAdapter(credentials))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            this.reportingClient = r;
          } catch (GeneralSecurityException e) {
            throw new IOException("Failed to build Play Developer Reporting client", e);
          }
        }
      }
    }
    return r;
  }

  private GoogleCredentials loadCredentials(String scope) throws IOException {
    try (var in = credentialsResource.getInputStream()) {
      return GoogleCredentials.fromStream(in).createScoped(Collections.singleton(scope));
    }
  }

  // ---------------------------------------------------------------------------
  // Edit lifecycle helper
  // ---------------------------------------------------------------------------

  private <T> T withEdit(String packageName, EditAction<T> action) throws IOException {
    AndroidPublisher.Edits edits = publisher().edits();
    AppEdit edit = edits.insert(packageName, null).execute();
    if (edit == null || edit.getId() == null) {
      throw new IOException("Google Play returned no edit id for package " + packageName);
    }
    String editId = edit.getId();
    try {
      return action.run(edits, editId);
    } finally {
      try {
        edits.delete(packageName, editId).execute();
      } catch (IOException cleanupEx) {
        log.warn(
            "Failed to delete Play edit {} for {}: {}",
            editId,
            packageName,
            cleanupEx.getMessage());
      }
    }
  }

  private String toJson(ObjectNode node) throws IOException {
    return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
  }

  @Override
  public String checkAppDetails(String packageName) throws IOException {
    ObjectNode root = objectMapper.createObjectNode();
    root.put("packageName", packageName);
    try {
      withEdit(packageName, (edits, editId) -> edits.tracks().list(packageName, editId).execute());
      root.put("success", true);
      root.put("message", "Umeunganishwa kwa mafanikio na App: " + packageName);
    } catch (IOException e) {
      log.warn("checkAppDetails failed for {}: {}", packageName, e.getMessage());
      root.put("success", false);
      root.put("message", "Imeshindikana kuunganisha: " + e.getMessage());
    }
    return toJson(root);
  }

  // ---------------------------------------------------------------------------
  // Public API
  // ---------------------------------------------------------------------------

  @Override
  public String fetchAppListingDetails(String packageName) throws IOException {
    return withEdit(
        packageName,
        (edits, editId) -> {
          TracksListResponse tracksResponse = edits.tracks().list(packageName, editId).execute();
          List<Track> tracks = tracksResponse == null ? null : tracksResponse.getTracks();

          ObjectNode root = objectMapper.createObjectNode();
          root.put("packageName", packageName);
          ArrayNode tracksArray = root.putArray("tracks");

          if (tracks != null) {
            for (Track track : tracks) {
              ObjectNode trackNode = tracksArray.addObject();
              trackNode.put("trackName", track.getTrack());
              ArrayNode releasesArray = trackNode.putArray("releases");

              if (track.getReleases() != null) {
                for (TrackRelease release : track.getReleases()) {
                  ObjectNode releaseNode = releasesArray.addObject();
                  releaseNode.put("status", release.getStatus());
                  ArrayNode versionCodes = releaseNode.putArray("versionCodes");
                  if (release.getVersionCodes() != null) {
                    release.getVersionCodes().forEach(versionCodes::add);
                  }
                  if (release.getUserFraction() != null) {
                    releaseNode.put("userFraction", release.getUserFraction());
                  } else {
                    releaseNode.putNull("userFraction");
                  }
                  releaseNode.put(
                      "releaseNotesCount",
                      release.getReleaseNotes() == null ? 0 : release.getReleaseNotes().size());
                }
              }
            }
          }

          if (tracks == null || tracks.isEmpty()) {
            root.put("note", "Hakuna Tracks zilizopatikana kwa app hii.");
          }
          return toJson(root);
        });
  }

  @Override
  public String fetchUploadedBundles(String packageName) throws IOException {
    return withEdit(
        packageName,
        (edits, editId) -> {
          BundlesListResponse bundlesResponse = edits.bundles().list(packageName, editId).execute();
          List<Bundle> bundles = bundlesResponse == null ? null : bundlesResponse.getBundles();

          ObjectNode root = objectMapper.createObjectNode();
          root.put("packageName", packageName);
          ArrayNode bundlesArray = root.putArray("bundles");
          if (bundles != null) {
            for (Bundle b : bundles) {
              ObjectNode node = bundlesArray.addObject();
              node.put("versionCode", b.getVersionCode() == null ? 0L : b.getVersionCode());
              node.put("sha1", b.getSha1());
            }
          }
          root.put("totalBundles", bundles == null ? 0 : bundles.size());
          root.put(
              "note",
              "Takwimu kamili za downloads hazipatikani kupitia Publishing API. "
                  + "Tumia Google Play Developer Reporting API au Play Console reports.");
          return toJson(root);
        });
  }

  @Override
  public String fetchErrorReportsAndVitals(String packageName) throws IOException {
    ObjectNode root = objectMapper.createObjectNode();
    root.put("packageName", packageName);

    try {
      Playdeveloperreporting reporting = reportingClient();

      // GET request (correct method per API docs)
      GooglePlayDeveloperReportingV1beta1SearchErrorReportsResponse response =
          reporting
              .vitals()
              .errors()
              .reports()
              .search("apps/" + packageName)
              .setPageSize(50)
              .execute();

      List<GooglePlayDeveloperReportingV1beta1ErrorReport> reports =
          response == null ? null : response.getErrorReports();

      ArrayNode reportsArray = root.putArray("errorReports");
      if (reports != null) {
        for (GooglePlayDeveloperReportingV1beta1ErrorReport report : reports) {
          ObjectNode node = reportsArray.addObject();
          node.put("name", report.getName());
          node.put("type", report.getType());
          if (report.getIssue() != null) {
            node.put("issue", report.getIssue());
          }
          if (report.getDeviceModel() != null) {
            node.put("deviceModel", report.getDeviceModel().toString());
          }
          if (report.getOsVersion() != null) {
            node.put("osVersion", report.getOsVersion().getApiLevel());
          }
          if (report.getAppVersion() != null) {
            node.put("versionCode", report.getAppVersion().getVersionCode());
          }
          //					node.put("createTime", report);
        }
      }

      root.put("totalReports", reports == null ? 0 : reports.size());
      root.put("success", true);
      root.put(
          "note",
          "Hii ni data ya error reports. Kwa vitals metrics (crash rate, ANR rate), "
              + "tumia endpoints za vitals.*.query.");

    } catch (Exception e) {
      log.warn("fetchErrorReportsAndVitals failed for {}: {}", packageName, e.getMessage());
      root.put("success", false);
      root.put("message", "Imeshindikana kuvuta taarifa za Reporting API: " + e.getMessage());
      root.put(
          "note",
          "Hakikisha huduma ya 'Google Play Developer Reporting API' imewashwa kwenye "
              + "Google Cloud Console na akaunti ina ruhusa sahihi.");
    }

    return toJson(root);
  }

  @FunctionalInterface
  private interface EditAction<T> {
    T run(AndroidPublisher.Edits edits, String editId) throws IOException;
  }
}
