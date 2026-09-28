package com.jemigraph.jemigraph_backend.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "azam.pesa")
@Data
public class AzamPesaConfig {
  private String appName;
  private String clientId;
  private String clientSecret;
  private String environment;
  private String authUrl;
  private String baseUrl;
}
