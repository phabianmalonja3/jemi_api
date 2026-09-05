package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.DTO.SmsRequestDto;
import com.jemigraph.jemigraph_backend.configs.WeblineConfig;
import com.jemigraph.jemigraph_backend.services.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final WeblineConfig weblineConfig;
    private final RestClient restClient;

    @Override
    public void sendSms(String recipient, String message) {
        String formattedRecipient = formatPhoneNumber(recipient);
        SmsRequestDto requestBody = new SmsRequestDto();
        requestBody.setApiToken(weblineConfig.getApiToken());
        requestBody.setRecipient(formattedRecipient);
        requestBody.setSenderId(weblineConfig.getSenderId());
        requestBody.setType(weblineConfig.getType());
        requestBody.setMessage(message);

        try {
            restClient.post()
                    .uri("/send")
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw new RuntimeException("Failed to send SMS via Webline: " + e.getMessage(), e);
        }
    }

    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return phone;
        }
        phone = phone.trim().replace("+", "");
        if (phone.startsWith("0")) {
            phone = "255" + phone.substring(1);
        }
        else if (!phone.startsWith("255")) {
            phone = "255" + phone;
        }
        return phone;
    }
}