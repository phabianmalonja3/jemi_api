package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.Entities.RequestStatus;
import com.jemigraph.jemigraph_backend.Entities.Subscription;
import com.jemigraph.jemigraph_backend.Entities.SubscriptionPlan;
import com.jemigraph.jemigraph_backend.Entities.SubscriptionRequest;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.enums.SubscriptionPlanType;
import com.jemigraph.jemigraph_backend.enums.SubscriptionStatus;
import com.jemigraph.jemigraph_backend.repositories.SubscriptionPlanRepository;
import com.jemigraph.jemigraph_backend.repositories.SubscriptionRepository;
import com.jemigraph.jemigraph_backend.repositories.SubscriptionRequestRepository;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.services.EmailService;
import com.jemigraph.jemigraph_backend.services.SubscriptionService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

  private final SubscriptionRequestRepository subscriptionRequestRepository;
  private final UserRepository userRepository;
  private final EmailService emailService;

  private final SubscriptionRepository subscriptionRepository;
  private final SubscriptionPlanRepository subscriptionPlanRepository;

  @Override
  @Transactional(readOnly = true)
  public Subscription getByUserId(UUID userId) {
    return subscriptionRepository.findByUserId(userId).orElse(null);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean hasActiveSubscription(UUID userId) {

    return subscriptionRepository
        .findByUserId(userId)
        .map(
            subscription ->
                subscription.getStatus() == SubscriptionStatus.ACTIVE
                    && subscription.getExpiresAt() != null
                    && subscription.getExpiresAt().isAfter(LocalDateTime.now()))
        .orElse(false);
  }

  // ============================================================
  // ACTIVATE / EXTEND BY EMAIL
  // ============================================================

  @Override
  @Transactional
  public void activateOrExtendSubscription(String email, int days) {

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

    Subscription subscription =
        subscriptionRepository
            .findByUserId(user.getId())
            .orElseThrow(
                () -> new RuntimeException("Subscription not found for user: " + user.getId()));

    LocalDateTime now = LocalDateTime.now();

    LocalDateTime baseDate =
        subscription.getExpiresAt() != null && subscription.getExpiresAt().isAfter(now)
            ? subscription.getExpiresAt()
            : now;

    subscription.setStatus(SubscriptionStatus.ACTIVE);
    subscription.setStartedAt(
        subscription.getStartedAt() != null ? subscription.getStartedAt() : now);
    subscription.setExpiresAt(baseDate.plusDays(days));

    subscriptionRepository.save(subscription);
  }

  @Override
  @Transactional
  public User activateSubscriptionForUser(UUID userId, UUID planId) {

    activateSubscription(userId, planId);

    return userRepository
        .findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
  }

  @Override
  @Transactional
  public void activateSubscription(UUID userId, UUID planId) {

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

    SubscriptionPlan plan =
        subscriptionPlanRepository
            .findById(planId)
            .orElseThrow(
                () -> new RuntimeException("Subscription plan not found with id: " + planId));

    int durationDays =
        plan.getDurationInDays() != null && plan.getDurationInDays() > 0
            ? plan.getDurationInDays()
            : 30;

    LocalDateTime now = LocalDateTime.now();

    Subscription subscription =
        subscriptionRepository
            .findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Subscription not found for user: " + userId));

    LocalDateTime baseDate =
        subscription.getExpiresAt() != null && subscription.getExpiresAt().isAfter(now)
            ? subscription.getExpiresAt()
            : now;

    SubscriptionPlan pkg =
        subscriptionPlanRepository
            .findByName(SubscriptionPlanType.valueOf(plan.getName().toString()))
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Package not found for subscription plan: " + plan.getName()));

    subscription.setSubscriptionPackage(pkg);
    subscription.setStatus(SubscriptionStatus.ACTIVE);
    subscription.setStartedAt(
        subscription.getStartedAt() != null ? subscription.getStartedAt() : now);

    subscription.setExpiresAt(baseDate.plusDays(durationDays));

    subscriptionRepository.save(subscription);

    System.out.println(
        "Subscription successfully activated for user: "
            + user.getEmail()
            + " expiring on: "
            + subscription.getExpiresAt());
  }

  @Override
  @Transactional
  public void sendSubscriptionApprovalRequest(UUID userId, UUID subscriptionPlanId) {

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

    SubscriptionPlan plan =
        subscriptionPlanRepository
            .findById(subscriptionPlanId)
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Subscription plan not found with id: " + subscriptionPlanId));

    int durationDays =
        plan.getDurationInDays() != null && plan.getDurationInDays() > 0
            ? plan.getDurationInDays()
            : 30;

    SubscriptionRequest request = new SubscriptionRequest();

    request.setUserId(userId);
    request.setSubscriptionPlanId(subscriptionPlanId);
    request.setStatus(RequestStatus.PENDING);

    SubscriptionRequest savedRequest = subscriptionRequestRepository.save(request);

    String userEmail = user.getEmail();
    String planName = String.valueOf(plan.getName());
    BigDecimal planPrice = plan.getPrice();

    UUID requestId = savedRequest.getId();

    emailService.sendApprovalNotificationToAdmin(
        userEmail, planName, planPrice, durationDays, requestId);
  }

  // ============================================================
  // ADMIN APPROVE / REJECT
  // ============================================================

  @Override
  @Transactional
  public void handleAdminAction(UUID id, boolean isApproved) {

    SubscriptionRequest request =
        subscriptionRequestRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Request not found"));

    User user =
        userRepository
            .findById(request.getUserId())
            .orElseThrow(
                () -> new RuntimeException("User not found with id: " + request.getUserId()));

    SubscriptionPlan plan =
        subscriptionPlanRepository
            .findById(request.getSubscriptionPlanId())
            .orElseThrow(() -> new RuntimeException("Subscription plan not found"));

    /*
     * If admin rejects the request,
     * do not activate subscription.
     */
    if (!isApproved) {

      request.setStatus(RequestStatus.REJECTED);

      subscriptionRequestRepository.save(request);

      return;
    }

    // ========================================================
    // APPROVED
    // ========================================================

    int durationDays =
        plan.getDurationInDays() != null && plan.getDurationInDays() > 0
            ? plan.getDurationInDays()
            : 30;

    LocalDateTime now = LocalDateTime.now();

    Subscription subscription =
        subscriptionRepository
            .findByUserId(user.getId())
            .orElseThrow(
                () -> new RuntimeException("Subscription not found for user: " + user.getId()));

    LocalDateTime baseDate =
        subscription.getExpiresAt() != null && subscription.getExpiresAt().isAfter(now)
            ? subscription.getExpiresAt()
            : now;

    /*
     * Map SubscriptionPlan -> Pkg
     */
    SubscriptionPlan pkg =
        subscriptionPlanRepository
            .findByName(SubscriptionPlanType.valueOf(plan.getName().toString()))
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Package not found for subscription plan: " + plan.getName()));

    subscription.setSubscriptionPackage(pkg);
    subscription.setStatus(SubscriptionStatus.ACTIVE);
    subscription.setStartedAt(now);
    subscription.setExpiresAt(baseDate.plusDays(durationDays));

    subscriptionRepository.save(subscription);

    request.setStatus(RequestStatus.APPROVED);

    subscriptionRequestRepository.save(request);
    String userEmail = user.getEmail();

    String userName = user.getName() != null ? user.getName() : "Valued Customer";

    String planName = plan.getName().toString();

    String amount = plan.getPrice() != null ? plan.getPrice().toString() : "0";

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    String startDate = now.format(formatter);

    String expiryDateStr = subscription.getExpiresAt().format(formatter);

    emailService.sendSubscriptionActivated(
        userEmail, userName, planName, amount, startDate, expiryDateStr);
  }
}
