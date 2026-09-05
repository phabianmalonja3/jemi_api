package com.jemigraph.jemigraph_backend.controllers;

import com.jemigraph.jemigraph_backend.DTO.LiveLocationUpdate;

import com.jemigraph.jemigraph_backend.services.LiveLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;

import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebsocketController {
    private final LiveLocationService liveLocationService;
    @MessageMapping("/location-update")
    public void handleMovement(@Payload LiveLocationUpdate update) {
        log.info("Live update: {} , {} ,{}", update.getLatitude(), update.getLongitude(),update.getUserId());
        liveLocationService.updateLiveLocation(update);
    }
}
