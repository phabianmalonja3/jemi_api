package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.services.SessionService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {
  /** How long a session remains active without refresh. */
  private static final Duration SESSION_TTL = Duration.ofDays(7);

  private static final String SESSION_PREFIX = "session:";
  private static final String USER_SESSION_PREFIX = "session:user:";
  private final StringRedisTemplate redisTemplate;

  // ============================================================
  // KEYS
  // ============================================================

  private String getSessionKey(String sessionId) {
    return SESSION_PREFIX + sessionId;
  }

  private String getUserSessionKey(UUID userId) {
    return USER_SESSION_PREFIX + userId;
  }

  // ============================================================
  // HAS ACTIVE SESSION
  // ============================================================

  @Override
  public boolean hasActiveSession(UUID userId) {

    if (userId == null) {
      return false;
    }

    String userSessionKey = getUserSessionKey(userId);

    String sessionId = redisTemplate.opsForValue().get(userSessionKey);

    if (sessionId == null || sessionId.isBlank()) {
      return false;
    }

    /*
     * Double check that the actual session
     * still exists.
     */
    return Boolean.TRUE.equals(redisTemplate.hasKey(getSessionKey(sessionId)));
  }

  // ============================================================
  // CREATE SESSION
  // ============================================================

  @Override
  public String createSession(UUID userId, String email, String deviceName) {

    if (userId == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    /*
     * Check if user already has an active session.
     */
    if (hasActiveSession(userId)) {

      throw new IllegalStateException("USER_ALREADY_HAS_ACTIVE_SESSION");
    }

    /*
     * Generate UUID session ID.
     */
    String sessionId = UUID.randomUUID().toString();

    String sessionKey = getSessionKey(sessionId);

    String userSessionKey = getUserSessionKey(userId);

    /*
     * Store complete session information.
     */
    HashOperations<String, String, String> hash = redisTemplate.opsForHash();

    hash.put(sessionKey, "userId", userId.toString());

    hash.put(sessionKey, "email", email != null ? email : "");

    hash.put(sessionKey, "deviceName", deviceName != null ? deviceName : "Unknown Device");

    hash.put(sessionKey, "createdAt", LocalDateTime.now().toString());

    hash.put(sessionKey, "lastActiveAt", LocalDateTime.now().toString());

    /*
     * Set expiration for the actual session.
     */
    redisTemplate.expire(sessionKey, SESSION_TTL);

    /*
     * Map user -> active session.
     */
    redisTemplate.opsForValue().set(userSessionKey, sessionId, SESSION_TTL);

    return sessionId;
  }

  // ============================================================
  // GET SESSION ID
  // ============================================================

  @Override
  public Optional<String> getSessionId(UUID userId) {

    if (userId == null) {
      return Optional.empty();
    }

    String sessionId = redisTemplate.opsForValue().get(getUserSessionKey(userId));

    return Optional.ofNullable(sessionId);
  }

  // ============================================================
  // GET COMPLETE SESSION
  // ============================================================

  @Override
  public Map<Object, Object> getSession(UUID userId) {

    if (userId == null) {
      return Collections.emptyMap();
    }

    Optional<String> sessionId = getSessionId(userId);

    if (sessionId.isEmpty()) {
      return Collections.emptyMap();
    }

    String sessionKey = getSessionKey(sessionId.get());

    Map<Object, Object> session = redisTemplate.opsForHash().entries(sessionKey);

    if (session == null || session.isEmpty()) {
      return Collections.emptyMap();
    }

    return session;
  }

  // ============================================================
  // VALIDATE SESSION
  // ============================================================

  @Override
  public boolean isValidSession(UUID userId, String sessionId) {

    if (userId == null || sessionId == null || sessionId.isBlank()) {
      return false;
    }

    Optional<String> storedSession = getSessionId(userId);

    if (storedSession.isEmpty()) {
      return false;
    }

    /*
     * Compare UUID session IDs.
     */
    if (!storedSession.get().equals(sessionId)) {
      return false;
    }

    /*
     * Make sure actual session still exists.
     */
    return Boolean.TRUE.equals(redisTemplate.hasKey(getSessionKey(sessionId)));
  }

  // ============================================================
  // INVALIDATE SESSION
  // ============================================================

  @Override
  public void invalidateSession(UUID userId) {

    if (userId == null) {
      return;
    }

    String userSessionKey = getUserSessionKey(userId);

    String sessionId = redisTemplate.opsForValue().get(userSessionKey);

    /*
     * Delete actual session.
     */
    if (sessionId != null && !sessionId.isBlank()) {

      redisTemplate.delete(getSessionKey(sessionId));
    }

    /*
     * Delete user -> session mapping.
     */
    redisTemplate.delete(userSessionKey);
  }

  // ============================================================
  // REFRESH SESSION
  // ============================================================

  @Override
  public void refreshSession(UUID userId) {

    if (userId == null) {
      return;
    }

    Optional<String> sessionId = getSessionId(userId);

    if (sessionId.isEmpty()) {
      return;
    }

    String sessionKey = getSessionKey(sessionId.get());

    /*
     * Make sure session still exists.
     */
    if (!Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey))) {
      return;
    }

    /*
     * Update last active time.
     */
    redisTemplate.opsForHash().put(sessionKey, "lastActiveAt", LocalDateTime.now().toString());

    /*
     * Extend session expiration.
     */
    redisTemplate.expire(sessionKey, SESSION_TTL);

    redisTemplate.expire(getUserSessionKey(userId), SESSION_TTL);
  }
}
