package com.jemigraph.jemigraph_backend.repositories;

import com.jemigraph.jemigraph_backend.Entities.Subscription;
import com.jemigraph.jemigraph_backend.enums.SubscriptionStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
  Optional<Subscription> findByUserId(UUID userId);

  Optional<Subscription> findByUserIdAndStatus(UUID userId, SubscriptionStatus status);

  boolean existsByUserId(UUID userId);

  List<Subscription> findByStatusAndExpiresAtBefore(
      SubscriptionStatus subscriptionStatus, LocalDateTime now);

  
}
