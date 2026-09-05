package com.jemigraph.jemigraph_backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    private final StringRedisTemplate redisTemplate;
    private static final int MAX_ATTEMPT = 5;
    private static final long LOCK_TIME_DURATION = 15; // Dakika 15

    private String getRedisKey(String email) {
        return "login_attempt:" + email;
    }

    public void loginSucceeded(String email) {
        redisTemplate.delete(getRedisKey(email));
    }

    public void loginFailed(String email) {
        String key = getRedisKey(email);
        String attempts = redisTemplate.opsForValue().get(key);
        int count = (attempts == null) ? 0 : Integer.parseInt(attempts);
        count++;

        redisTemplate.opsForValue().set(key, String.valueOf(count), Duration.ofMinutes(LOCK_TIME_DURATION));
    }

    public boolean isBlocked(String email) {
        String key = getRedisKey(email);
        String attempts = redisTemplate.opsForValue().get(key);
        return attempts != null && Integer.parseInt(attempts) >= MAX_ATTEMPT;
    }
}