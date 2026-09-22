package com.jemigraph.jemigraph_backend.jobs;

import com.jemigraph.jemigraph_backend.Entities.Subscription;
import com.jemigraph.jemigraph_backend.enums.SubscriptionStatus;
import com.jemigraph.jemigraph_backend.repositories.SubscriptionRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {

    private final SubscriptionRepository subscriptionRepository;

    /**
     * Runs every day at midnight.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void checkAndExpireSubscriptions() {

        log.info(
                "Running scheduled job: Checking for expired subscriptions..."
        );

        LocalDateTime now = LocalDateTime.now();

        List<Subscription> expiredSubscriptions =
                subscriptionRepository.findByStatusAndExpiresAtBefore(
                        SubscriptionStatus.ACTIVE,
                        now
                );

        if (expiredSubscriptions.isEmpty()) {
            log.info("No expired subscriptions found.");
            return;
        }

        for (Subscription subscription : expiredSubscriptions) {

            subscription.setStatus(SubscriptionStatus.EXPIRED);

            log.info(
                    "Subscription expired for user: {}",
                    subscription.getUser().getEmail()
            );
        }

        subscriptionRepository.saveAll(expiredSubscriptions);

        log.info(
                "Successfully updated {} expired subscription(s).",
                expiredSubscriptions.size()
        );
    }
}