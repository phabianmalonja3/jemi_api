package com.jemigraph.jemigraph_backend.services;

import org.springframework.stereotype.Service;

@Service
public interface SmsService {
    void sendSms(String recipient, String message);
}
