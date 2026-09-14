package com.jemigraph.jemigraph_backend.controllers;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.jemigraph.jemigraph_backend.services.GooglePlayService;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/google-play")
@RequiredArgsConstructor
public class GooglePlayController {

  private static final Logger log = LoggerFactory.getLogger(GooglePlayController.class);

  private static final Pattern PACKAGE_NAME_PATTERN =
      Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)+$");

  private final GooglePlayService googlePlayService;

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private static boolean isBlank(String s) {
    return s == null || s.isBlank();
  }

  private static String normalize(String s) {
    return s == null ? null : s.trim();
  }

  private static boolean isValidPackageName(String s) {
    return s != null && PACKAGE_NAME_PATTERN.matcher(s).matches();
  }

  private static String jsonEscape(String s) {
    if (s == null) return "";
    StringBuilder sb = new StringBuilder(s.length() + 16);
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '"' -> sb.append("\\\"");
        case '\\' -> sb.append("\\\\");
        case '\n' -> sb.append("\\n");
        case '\r' -> sb.append("\\r");
        case '\t' -> sb.append("\\t");
        default -> {
          if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
          else sb.append(c);
        }
      }
    }
    return sb.toString();
  }

  private static ResponseEntity<String> jsonError(HttpStatus status, String message) {
    return ResponseEntity.status(status)
        .contentType(MediaType.APPLICATION_JSON)
        .body("{\"error\":\"" + jsonEscape(message) + "\"}");
  }

  private static ResponseEntity<String> validatePackage(String pkg) {
    if (isBlank(pkg)) {
      return jsonError(HttpStatus.BAD_REQUEST, "packageName haitakiwi kuwa tupu.");
    }
    if (!isValidPackageName(pkg)) {
      return jsonError(
          HttpStatus.BAD_REQUEST,
          "packageName sio sahihi: '" + pkg + "'. Mfano halali: com.example.app");
    }
    return null;
  }

  private static HttpStatus mapGoogleStatus(int code) {
    return switch (code) {
      case 400 -> HttpStatus.BAD_REQUEST;
      case 401 -> HttpStatus.UNAUTHORIZED;
      case 403 -> HttpStatus.FORBIDDEN;
      case 404 -> HttpStatus.NOT_FOUND;
      case 429 -> HttpStatus.TOO_MANY_REQUESTS;
      default -> HttpStatus.BAD_GATEWAY;
    };
  }

  private static ResponseEntity<String> handleGoogleError(String method, String pkg, Exception e) {
    if (e instanceof GoogleJsonResponseException g) {
      String msg = g.getDetails() == null ? e.getMessage() : g.getDetails().getMessage();
      log.warn("{} Google error {} for {}: {}", method, g.getStatusCode(), pkg, msg);
      return jsonError(mapGoogleStatus(g.getStatusCode()), "Google Play: " + msg);
    }
    log.warn("{} IO error for {}: {}", method, pkg, e.getMessage());
    return jsonError(HttpStatus.BAD_GATEWAY, "Hitilafu ya mawasiliano: " + e.getMessage());
  }

  // ---------------------------------------------------------------------------
  // Endpoints
  // ---------------------------------------------------------------------------

  @GetMapping(value = "/check-status", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> checkAppStatus(@RequestParam String packageName) {
    String pkg = normalize(packageName);
    ResponseEntity<String> err = validatePackage(pkg);
    if (err != null) return err;

    try {
      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_JSON)
          .body(googlePlayService.checkAppDetails(pkg));
    } catch (Exception e) {
      return handleGoogleError("checkAppStatus", pkg, e);
    }
  }

  @GetMapping(value = "/app-details", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getAppDetails(@RequestParam String packageName) {
    String pkg = normalize(packageName);
    ResponseEntity<String> err = validatePackage(pkg);
    if (err != null) return err;

    try {
      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_JSON)
          .body(googlePlayService.fetchAppListingDetails(pkg));
    } catch (Exception e) {
      return handleGoogleError("getAppDetails", pkg, e);
    }
  }

  @GetMapping(value = "/bundles", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getUploadedBundles(@RequestParam String packageName) {
    String pkg = normalize(packageName);
    ResponseEntity<String> err = validatePackage(pkg);
    if (err != null) return err;

    try {
      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_JSON)
          .body(googlePlayService.fetchUploadedBundles(pkg));
    } catch (Exception e) {
      return handleGoogleError("getUploadedBundles", pkg, e);
    }
  }

  @GetMapping(value = "/vitals", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getErrorReportsAndVitals(@RequestParam String packageName) {
    String pkg = normalize(packageName);
    ResponseEntity<String> err = validatePackage(pkg);
    if (err != null) return err;

    try {
      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_JSON)
          .body(googlePlayService.fetchErrorReportsAndVitals(pkg));
    } catch (Exception e) {
      return handleGoogleError("getErrorReportsAndVitals", pkg, e);
    }
  }
}
