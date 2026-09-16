package com.jemigraph.jemigraph_backend.services;

import java.io.IOException;

public interface GooglePlayService {
  String checkAppDetails(String packageName) throws IOException;
  String fetchAppListingDetails(String packageName) throws IOException;
  String fetchUploadedBundles(String packageName) throws IOException;
  String fetchErrorReportsAndVitals(String packageName) throws IOException;
}
