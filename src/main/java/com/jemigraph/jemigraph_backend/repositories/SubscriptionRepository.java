package com.jemigraph.jemigraph_backend.repositories;

import com.jemigraph.jemigraph_backend.Entities.Subscription;
import com.jemigraph.jemigraph_backend.enums.SubscriptionStatus;
import io.lettuce.core.dynamic.annotation.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
  Optional<Subscription> findByUserId(UUID userId);

  Optional<Subscription> findByUserIdAndStatus(UUID userId, SubscriptionStatus status);

  boolean existsByUserId(UUID userId);

  @Query(
      """
        SELECT s
        FROM Subscription s
        JOIN FETCH s.user u
        JOIN FETCH s.subscriptionPackage p
        WHERE s.status = :status
        AND s.expiresAt BETWEEN :start AND :end
    """)
  List<Subscription> findSubscriptionsExpiringBetween(
      @Param("status") SubscriptionStatus status,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  List<Subscription> findByStatusAndExpiresAtBetween(
      SubscriptionStatus status, LocalDateTime start, LocalDateTime end);

  List<Subscription> findByStatusAndExpiresAtBefore(
      SubscriptionStatus subscriptionStatus, LocalDateTime now);
}
