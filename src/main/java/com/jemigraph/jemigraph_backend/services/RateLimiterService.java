package com.jemigraph.jemigraph_backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RedisTemplate<String, Object> redisTemplate;

    public boolean isAllowed(String clientIp) {
        String key = "rate_limit:" + clientIp;

        // 1. Tumia increment moja kwa moja.
        // Hii ni "Atomic Operation" - ni salama na haina Casting issues.
        // Kama key haipo, Redis itatengeneza na kuanza na 1.
        Long count = redisTemplate.opsForValue().increment(key);

        // 2. Kama ndio kwanza key imetengenezwa (count == 1), weka muda wa ku-expire
        if (count != null && count == 1) {
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
        }

        // 3. Linganisha: Je, imevuka mara 10?
        // Tunarudisha true kama bado hajafika 10, false kama amevuka.
        return count != null && count <= 10;
    }
}