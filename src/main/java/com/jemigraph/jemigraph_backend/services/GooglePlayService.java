package com.jemigraph.jemigraph_backend.services;

import com.google.api.services.androidpublisher.AndroidPublisher;
import org.springframework.stereotype.Service;

@Service
public interface GooglePlayService {
  AndroidPublisher getAndroidPublisherService() throws Exception;

  String checkAppDetails(String packageName);

  String fetchAppListingDetails(String packageName) throws Exception;

  // Njia mpya kwa ajili ya vipakuliwa na takwimu za kina
  String fetchDownloadStatistics(String packageName) throws Exception;
}
