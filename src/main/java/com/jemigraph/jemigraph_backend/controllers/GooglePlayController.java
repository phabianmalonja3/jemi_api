package com.jemigraph.jemigraph_backend.controllers;

import com.jemigraph.jemigraph_backend.services.GooglePlayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/google-play")
@RequiredArgsConstructor
public class GooglePlayController {

  private final GooglePlayService googlePlayService;

  @GetMapping("/check-status")
  public ResponseEntity<String> checkAppStatus(@RequestParam String packageName) {
    String result = googlePlayService.checkAppDetails(packageName);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/app-details")
  public ResponseEntity<String> getAppDetails(@RequestParam String packageName) {
    try {
      String details = googlePlayService.fetchAppListingDetails(packageName);
      return ResponseEntity.ok(details);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body("Hitilafu: " + e.getMessage());
    }
  }

  @GetMapping("/download-stats")
  public ResponseEntity<String> getDownloadStats(@RequestParam String packageName) {
    try {
      String stats = googlePlayService.fetchDownloadStatistics(packageName);
      return ResponseEntity.ok(stats);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body("Hitilafu: " + e.getMessage());
    }
  }
}
