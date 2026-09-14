package com.jemigraph.jemigraph_backend.services;

import java.io.IOException;

public interface GooglePlayService {

  /** Verifies that the configured service account can reach the given app. */
  String checkAppDetails(String packageName) throws IOException;

  /** Fetches tracks + releases for the given app. */
  String fetchAppListingDetails(String packageName) throws IOException;

  /**
   * Lists uploaded AAB bundles for the given app. NOTE: The Android Publisher API does NOT expose
   * download counts.
   */
  String fetchUploadedBundles(String packageName) throws IOException;

  /**
   * Fetches error reports and vitals metrics for the given app using the Google Play Developer
   * Reporting API.
   */
  String fetchErrorReportsAndVitals(String packageName) throws IOException;
}
