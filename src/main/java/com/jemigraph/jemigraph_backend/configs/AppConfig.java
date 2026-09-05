package com.jemigraph.jemigraph_backend.configs;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "web")

public class AppConfig {
    private String frontendUrl;
}
