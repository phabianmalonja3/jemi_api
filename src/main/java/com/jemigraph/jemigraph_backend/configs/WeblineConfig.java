package com.jemigraph.jemigraph_backend.configs;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Data
@Component
@ConfigurationProperties(prefix = "webline.sms")
public class WeblineConfig {
    private String apiToken;
    private String baseUrl;
    private String senderId;
    private String type;
}
