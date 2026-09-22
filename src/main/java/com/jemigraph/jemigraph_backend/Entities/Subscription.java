package com.jemigraph.jemigraph_backend.Entities;

import com.jemigraph.jemigraph_backend.enums.SubscriptionStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(
    name = "subscriptions",
    uniqueConstraints = {@UniqueConstraint(name = "uk_subscription_user", columnNames = "user_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "package_id", nullable = false)
  private SubscriptionPlan subscriptionPackage;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SubscriptionStatus status;

  @Column(name = "started_at", nullable = false)
  private LocalDateTime startedAt;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    LocalDateTime now = LocalDateTime.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
