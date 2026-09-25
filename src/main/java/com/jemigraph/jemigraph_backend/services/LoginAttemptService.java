package com.jemigraph.jemigraph_backend.services;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {
  private static final int MAX_ATTEMPT = 5;
  private static final long LOCK_TIME_DURATION = 15; // Dakika 15
  private final StringRedisTemplate redisTemplate;

  private String getRedisKey(String email) {
    return "login_attempt:" + email;
  }

  private String getAdminBlockKey(String email) {
    return "account:blocked:" + email;
  }

  public void loginSucceeded(String email) {
    redisTemplate.delete(getRedisKey(email));
  }

  public void loginFailed(String email) {
    if (isAdminBlocked(email)) {
      return;
    }

    String key = getRedisKey(email);
    String attempts = redisTemplate.opsForValue().get(key);
    int count = (attempts == null) ? 0 : Integer.parseInt(attempts);
    count++;

    redisTemplate
        .opsForValue()
        .set(key, String.valueOf(count), Duration.ofMinutes(LOCK_TIME_DURATION));
  }

  public boolean isAdminBlocked(String email) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(getAdminBlockKey(email)));
  }

  public boolean isBlocked(String email) {

    if (isAdminBlocked(email)) {
      return true;
    }

    String key = getRedisKey(email);
    String attempts = redisTemplate.opsForValue().get(key);
    return attempts != null && Integer.parseInt(attempts) >= MAX_ATTEMPT;
  }
}
