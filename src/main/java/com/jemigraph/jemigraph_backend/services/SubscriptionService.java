package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.Entities.Subscription;
import com.jemigraph.jemigraph_backend.Entities.User;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public interface SubscriptionService {

  @Transactional
  void activateOrExtendSubscription(String email, int days);

  Subscription getByUserId(UUID userId);

  boolean hasActiveSubscription(UUID userId);

  User activateSubscriptionForUser(UUID userId, UUID planId);

  void activateSubscription(UUID userId, UUID planId);

  void sendSubscriptionApprovalRequest(UUID userId, UUID subscriptionPlanId);

  void handleAdminAction(UUID id, boolean isApproved);
}
