package com.jemigraph.jemigraph_backend.listeners;

import com.jemigraph.jemigraph_backend.services.LiveStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeyExpiredListener implements MessageListener {

    private final LiveStatusService liveService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("🔔 Redis key expired: {}", expiredKey);

        if (expiredKey.startsWith("photographer:online:")) {
            String userId = expiredKey.substring("photographer:online:".length());
            log.info("📴 Photographer with ID {} did not renew heartbeat. Setting offline...", userId);

            try {
                liveService.handleExpiredLiveSession(userId);
            } catch (Exception e) {
                log.error("❌ Failed to handle expired live session for user ID {}: {}", userId, e.getMessage(), e);
            }
        }
    }
}