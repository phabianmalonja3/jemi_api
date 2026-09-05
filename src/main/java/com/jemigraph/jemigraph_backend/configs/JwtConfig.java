package com.jemigraph.jemigraph_backend.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtConfig {
    private  String secret;
    private Long expiration;
    private Long refreshTokenExpiration;
}
