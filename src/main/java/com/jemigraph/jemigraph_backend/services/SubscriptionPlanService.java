package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.Entities.SubscriptionPlan;
import com.jemigraph.jemigraph_backend.enums.SubscriptionPlanType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface SubscriptionPlanService {
    List<SubscriptionPlan> getAllActivePlans();
    SubscriptionPlan getPlanByType(SubscriptionPlanType planType);
    SubscriptionPlan createPlan(SubscriptionPlan plan);
    SubscriptionPlan updatePlan(UUID id, SubscriptionPlan planDetails);
    void deletePlan(UUID id);
}