package com.jemigraph.jemigraph_backend.services;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface SessionService {

  /** Check whether the user has an active session. */
  boolean hasActiveSession(UUID userId);

  /**
   * Create a new session for the user.
   *
   * @return generated session ID
   */
  String createSession(UUID userId, String email, String deviceName);

  /** Get active session ID. */
  Optional<String> getSessionId(UUID userId);

  /** Get complete session information. */
  Map<Object, Object> getSession(UUID userId);

  /** Validate a session against the stored session. */
  boolean isValidSession(UUID userId, String sessionId);

  /** Remove/invalidate active session. */
  void invalidateSession(UUID userId);

  /** Extend session expiration time. */
  void refreshSession(UUID userId);
}
