package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.Entities.User;
import java.util.UUID;

public interface SubscriptionService {
  void activateOrExtendSubscription(String email, int days);

  boolean isSubscriptionValid(User user);

  User activateSubscriptionForUser(UUID userId, UUID planId);

  void activateSubscription(UUID userId, UUID planId);

  void sendSubscriptionApprovalRequest(UUID userId, UUID subscriptionPlanId);

  void handleAdminAction(UUID id, boolean isApproved);
}
