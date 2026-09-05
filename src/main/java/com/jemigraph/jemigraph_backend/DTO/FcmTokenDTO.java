package com.jemigraph.jemigraph_backend.DTO;

import lombok.Data;

@Data
public class FcmTokenDTO {
    private String token;
    private String deviceType; // Mfano: "ANDROID" au "IOS"
}
