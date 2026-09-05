package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.Entities.SubscriptionPlan;

import com.jemigraph.jemigraph_backend.enums.SubscriptionPlanType;
import com.jemigraph.jemigraph_backend.repositories.SubscriptionPlanRepository;
import com.jemigraph.jemigraph_backend.services.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    @Override
    public List<SubscriptionPlan> getAllActivePlans() {
        return subscriptionPlanRepository.findByActiveTrue();
    }
    @Override
    public SubscriptionPlan getPlanByType(SubscriptionPlanType planType) {
        return subscriptionPlanRepository.findByName(planType)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found for type: " + planType));
    }
    @Override
    public SubscriptionPlan createPlan(SubscriptionPlan plan) {
        if (plan.getDurationInDays() == null && plan.getName() != null) {
            plan.setDurationInDays(plan.getName().getDurationInDays());
        }
        return subscriptionPlanRepository.save(plan);
    }

    @Override
    public SubscriptionPlan updatePlan(UUID id, SubscriptionPlan planDetails) {
        SubscriptionPlan existingPlan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription Plan not found with ID: " + id));

        existingPlan.setName(planDetails.getName());
        existingPlan.setPrice(planDetails.getPrice());
        existingPlan.setDescription(planDetails.getDescription());
        existingPlan.setActive(planDetails.isActive());

        if (planDetails.getName() != null) {
            existingPlan.setDurationInDays(planDetails.getName().getDurationInDays());
        }

        return subscriptionPlanRepository.save(existingPlan);
    }

    @Override
    public void deletePlan(UUID id) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription Plan not found with ID: " + id));
        plan.setActive(false);
        subscriptionPlanRepository.save(plan);
    }
}