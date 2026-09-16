package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.Entities.RequestStatus;
import com.jemigraph.jemigraph_backend.Entities.SubscriptionPlan;
import com.jemigraph.jemigraph_backend.Entities.SubscriptionRequest;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.enums.SubscriptionStatus;
import com.jemigraph.jemigraph_backend.enums.UserRole;
import com.jemigraph.jemigraph_backend.repositories.SubscriptionPlanRepository;
import com.jemigraph.jemigraph_backend.repositories.SubscriptionRequestRepository;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.services.EmailService;
import com.jemigraph.jemigraph_backend.services.SubscriptionService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

  private final SubscriptionPlanRepository subscriptionPlanRepository;

  @Override
  @Transactional
  public void activateOrExtendSubscription(String email, int days) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

    LocalDateTime currentExpiry = user.getSubscriptionExpiresAt();
    LocalDateTime baseTime =
        (currentExpiry != null && currentExpiry.isAfter(LocalDateTime.now()))
            ? currentExpiry
            : LocalDateTime.now();

    user.setSubscriptionExpiresAt(baseTime.plusDays(days));
    user.setSubscriptionStatus(SubscriptionStatus.ACTIVE);

    userRepository.save(user);
  }

  @Override
  public boolean isSubscriptionValid(User user) {
    if (user.getRole() == null || user.getRole() != UserRole.PHOTOGRAPHER) {
      return true;
    }

    return user.getSubscriptionStatus() == SubscriptionStatus.ACTIVE
        && user.getSubscriptionExpiresAt() != null
        && user.getSubscriptionExpiresAt().isAfter(LocalDateTime.now());
  }

  @Override
  @Transactional
  public User activateSubscriptionForUser(UUID userId, UUID planId) {

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

    SubscriptionPlan plan =
        subscriptionPlanRepository
            .findById(planId)
            .orElseThrow(
                () -> new RuntimeException("Subscription plan not found with ID: " + planId));

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime currentExpiry = user.getSubscriptionExpiresAt();
    LocalDateTime newExpiry;
    if (currentExpiry != null && currentExpiry.isAfter(now)) {
      newExpiry = currentExpiry.plusDays(plan.getDurationInDays());
    } else {
      newExpiry = now.plusDays(plan.getDurationInDays());
    }

    user.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
    user.setSubscriptionPlan(plan);
    user.setSubscriptionExpiresAt(newExpiry);

    return userRepository.save(user);
  }

  @Override
  @Transactional
  public void activateSubscription(UUID userId, UUID planId) {
    // 1. Tafuta mtumiaji
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
        plan.getDurationInDays() > 0 ? plan.getDurationInDays() : 30; // Default siku 30 kama haipo

    LocalDateTime expiryDate;

    if (user.getSubscriptionExpiresAt() != null
        && user.getSubscriptionExpiresAt().isAfter(LocalDateTime.now())) {
      expiryDate = user.getSubscriptionExpiresAt().plusDays(durationDays);
    } else {
      expiryDate = LocalDateTime.now().plusDays(durationDays);
    }

    user.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
    user.setSubscriptionExpiresAt(expiryDate);
    user.setSubscriptionPlan(plan);

    userRepository.save(user);

    System.out.println(
        "✅ Subscription successfully activated for user: "
            + user.getEmail()
            + " expiring on: "
            + expiryDate);
  }

  @Override
  @Transactional
  public void sendSubscriptionApprovalRequest(UUID userId, UUID subscriptionPlanId) {
    // 1. Tafuta User
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

    // 2. Tafuta Subscription Plan
    SubscriptionPlan plan =
        subscriptionPlanRepository
            .findById(subscriptionPlanId)
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Subscription plan not found with id: " + subscriptionPlanId));

    int durationDays = plan.getDurationInDays() > 0 ? plan.getDurationInDays() : 30;

    SubscriptionRequest request = new SubscriptionRequest();
    request.setUserId(userId);
    request.setSubscriptionPlanId(subscriptionPlanId);
    request.setStatus(RequestStatus.PENDING);

    SubscriptionRequest savedRequest = subscriptionRequestRepository.save(request);
    String userEmail = user.getEmail(); // Au username
    String planName = String.valueOf(plan.getName());
    BigDecimal planPrice = plan.getPrice();
    UUID requestId = savedRequest.getId();
    emailService.sendApprovalNotificationToAdmin(
        userEmail, planName, planPrice, durationDays, requestId);
  }

  @Override
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
    int durationDays =
        plan.getDurationInDays() > 0 ? plan.getDurationInDays() : 30; // Default siku 30 kama haipo
    LocalDateTime expiryDate;
    if (user.getSubscriptionExpiresAt() != null
        && user.getSubscriptionExpiresAt().isAfter(LocalDateTime.now())) {
      expiryDate = user.getSubscriptionExpiresAt().plusDays(durationDays);
    } else {
      expiryDate = LocalDateTime.now().plusDays(durationDays);
    }

    user.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
    user.setSubscriptionExpiresAt(expiryDate);
    user.setSubscriptionPlan(plan);
    userRepository.save(user);
    System.out.println(
        "✅ Subscription successfully activated for user: "
            + user.getEmail()
            + " expiring on: "
            + expiryDate);
  }
}
