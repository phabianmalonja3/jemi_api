package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.Entities.SubscriptionPlan;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.enums.SubscriptionPlanType;
import com.jemigraph.jemigraph_backend.enums.SubscriptionStatus;
import com.jemigraph.jemigraph_backend.enums.UserRole;
import com.jemigraph.jemigraph_backend.repositories.SubscriptionPlanRepository;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.services.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final UserRepository userRepository;
  
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Override
    @Transactional
    public void activateOrExtendSubscription(String email, int days) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        LocalDateTime currentExpiry = user.getSubscriptionExpiresAt();
        LocalDateTime baseTime = (currentExpiry != null && currentExpiry.isAfter(LocalDateTime.now()))
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

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found with ID: " + planId));

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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // 2. Tafuta plan ya usajili ili ujue muda wake (k.o. siku 30, siku 365, n.k.)
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found with id: " + planId));

        // 3. Kokotoa tarehe ya kumalizika muda wake (Mfano: kulingana na duration ya plan katika siku)
        int durationDays = plan.getDurationInDays() > 0 ? plan.getDurationInDays() : 30; // Default siku 30 kama haipo

        LocalDateTime expiryDate;

        if (user.getSubscriptionExpiresAt() != null && user.getSubscriptionExpiresAt().isAfter(LocalDateTime.now())) {
            expiryDate = user.getSubscriptionExpiresAt().plusDays(durationDays);
        } else {
            expiryDate = LocalDateTime.now().plusDays(durationDays);
        }

        user.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        user.setSubscriptionExpiresAt(expiryDate);
        user.setSubscriptionPlan(plan);

        // 5. Hifadhi kwenye database
        userRepository.save(user);

        // Debug log ya uhakika
        System.out.println("✅ Subscription successfully activated for user: " + user.getEmail() + " expiring on: " + expiryDate);
    }


}