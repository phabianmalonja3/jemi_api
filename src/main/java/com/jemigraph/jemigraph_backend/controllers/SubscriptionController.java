package com.jemigraph.jemigraph_backend.controllers;

import com.jemigraph.jemigraph_backend.Entities.SubscriptionPlan;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.enums.SubscriptionPlanType;
import com.jemigraph.jemigraph_backend.services.SubscriptionPlanService;
import com.jemigraph.jemigraph_backend.services.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/subscription-plans")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionPlanService subscriptionPlanService;
    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<List<SubscriptionPlan>> getAvailablePlans() {
        return ResponseEntity.ok(subscriptionPlanService.getAllActivePlans());
    }
    @PostMapping("/activate")
    public ResponseEntity<User> activateSubscription(
            @RequestParam UUID userId,
            @RequestParam UUID planTypeId) {
        User updatedUser = subscriptionService.activateSubscriptionForUser(userId, planTypeId);
        return ResponseEntity.ok(updatedUser);
    }
}