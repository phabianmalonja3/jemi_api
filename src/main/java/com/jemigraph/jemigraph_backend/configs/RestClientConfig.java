package com.jemigraph.jemigraph_backend.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
        @Bean
        public RestClient restClient() {
            return RestClient.builder()
                    .baseUrl("https://sms.webline.co.tz/api/http/sms")
                    .defaultHeader("Content-Type", "application/json")
                    .defaultHeader("Accept", "application/json")
                    .build();
        }

}