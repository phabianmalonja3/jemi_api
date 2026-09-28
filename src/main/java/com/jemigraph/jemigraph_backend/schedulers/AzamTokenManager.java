package com.jemigraph.jemigraph_backend.schedulers;

import com.jemigraph.jemigraph_backend.configs.AzamPesaConfig;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AzamTokenManager {

  private final AzamPesaConfig azamPesaConfig;
  private final RestTemplate restTemplate;

  private String cachedToken;
  private String cachedPublicKey;

  @PostConstruct
  public void init() {
    refreshAccessToken();
    refreshPublicKey();
  }

  @Scheduled(fixedRate = 86400000)
  public void refreshAccessToken() {
    try {
      String url = azamPesaConfig.getAuthUrl() + "/AppRegistration/GenerateToken";

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
      headers.set("User-Agent", "Jemigraph-Backend-Service"); // Inazuia server kukataa ombi

      Map<String, String> requestBody = new HashMap<>();
      requestBody.put("appName", azamPesaConfig.getAppName());
      requestBody.put("clientId", azamPesaConfig.getClientId());
      requestBody.put("clientSecret", azamPesaConfig.getClientSecret());

      HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

      // Tunatumia ResponseEntity<Map> kama kawaida
      ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        Map responseBody = response.getBody();

        // Tunahakikisha inasoma 'success' ikiwa true
        Boolean success = (Boolean) responseBody.get("success");
        if (success != null && success) {
          Map<String, Object> dataMap = (Map<String, Object>) responseBody.get("data");
          if (dataMap != null && dataMap.containsKey("accessToken")) {
            this.cachedToken = (String) dataMap.get("accessToken");
            log.info(
                "Token ya AzamPay imesasishwa na kutunzwa kikamilifu kwenye RAM kupitia Scheduler!");
            refreshPublicKey();
          } else {
            log.warn("Sehemu ya 'accessToken' haikuthibitika kwenye jibu la AzamPay.");
          }
        } else {
          log.warn(
              "AzamPay imerudisha mafanikio ya uongo (success = false): {}",
              responseBody.get("message"));
        }
      } else {
        log.warn(
            "Kujaribu kupata token kumeshindwa, jibu halikuwa la mafanikio. Status: {}",
            response.getStatusCode());
      }
    } catch (Exception e) {
      log.error(
          "Hitilafu imetokea wakati wa kuregenerate token ya AzamPay kwenye scheduler: {}",
          e.getMessage());
    }
  }

  @Scheduled(fixedRate = 86400000)
  public void refreshPublicKey() {
    try {
      if (this.cachedToken == null || this.cachedToken.isEmpty()) {
        return;
      }

      String url = azamPesaConfig.getAuthUrl() + "/azampay/v1/public-key?format=Pem";

      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(this.cachedToken);

      HttpEntity<Void> entity = new HttpEntity<>(headers);
      ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        this.cachedPublicKey = (String) response.getBody().get("publicKey");
        log.info("AzamPay Public Key imesasishwa na kutunzwa kwenye RAM kwa mafanikio!");
      }
    } catch (Exception e) {
      log.error("Hitilafu imetokea wakati wa kuchota Public Key: {}", e.getMessage());
    }
  }

  // Njia ya kupata Token iliyopo kwenye RAM
  public String getActiveToken() {
    if (this.cachedToken == null || this.cachedToken.isEmpty()) {
      log.warn("Token haipo kwenye RAM, inaombwa mara moja...");
      refreshAccessToken();
    }
    return this.cachedToken;
  }

  // Njia ya kupata Public Key iliyotunzwa kwenye RAM
  public String getActivePublicKey() {
    if (this.cachedPublicKey == null || this.cachedPublicKey.isEmpty()) {
      log.warn("Public Key haipo kwenye RAM, inaombwa mara moja...");
      refreshPublicKey();
    }
    return this.cachedPublicKey;
  }
}
