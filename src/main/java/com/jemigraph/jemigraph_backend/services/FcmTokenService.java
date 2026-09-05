package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.DTO.FcmTokenDTO;
import org.springframework.stereotype.Service;

@Service
public interface FcmTokenService {
    void updateFcmToken(String email, FcmTokenDTO fcmTokenDTO);
}

