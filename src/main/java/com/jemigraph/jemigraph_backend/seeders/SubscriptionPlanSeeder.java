package com.jemigraph.jemigraph_backend.seeders;

import com.jemigraph.jemigraph_backend.Entities.SubscriptionPlan;

import com.jemigraph.jemigraph_backend.enums.SubscriptionPlanType;
import com.jemigraph.jemigraph_backend.repositories.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class SubscriptionPlanSeeder implements CommandLineRunner {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Override
    public void run(String... args) throws Exception {

        createPlanIfNotExists(
                SubscriptionPlanType.MONTHLY,
                BigDecimal.valueOf(15000),
                "Access to all features for 1 month"
        );


        createPlanIfNotExists(
                SubscriptionPlanType.QUARTERLY,
                BigDecimal.valueOf(40000),
                "Access to all features for 3 months"
        );

        createPlanIfNotExists(
                SubscriptionPlanType.ANNUAL,
                BigDecimal.valueOf(120000),
                "Access to all features for 1 full year"
        );
    }

    private void createPlanIfNotExists(SubscriptionPlanType planType, BigDecimal defaultPrice, String description) {
        if (subscriptionPlanRepository.findByName(planType).isEmpty()) {
            SubscriptionPlan plan = SubscriptionPlan.builder()
                    .name(planType)
                    .price(defaultPrice)
                    .durationInDays(planType.getDurationInDays())
                    .description(description)
                    .active(true)
                    .build();

            subscriptionPlanRepository.save(plan);
            System.out.println("Created default subscription plan: " + planType);
        } else {
            System.out.println("Subscription plan " + planType + " already exists. Skipping seeder overwrite.");
        }
    }
}