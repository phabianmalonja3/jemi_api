package com.jemigraph.jemigraph_backend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SmsRequestDto {
    @JsonProperty("api_token")
    private String apiToken;
    private String recipient;
    @JsonProperty("sender_id")
    private String senderId;
    private String type;
    private String message;
}
