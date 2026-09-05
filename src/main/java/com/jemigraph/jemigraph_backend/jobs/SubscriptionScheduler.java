package com.jemigraph.jemigraph_backend.jobs;

import com.jemigraph.jemigraph_backend.Entities.User;

import com.jemigraph.jemigraph_backend.enums.SubscriptionStatus;
import com.jemigraph.jemigraph_backend.enums.UserRole;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {

    private final UserRepository userRepository;

    /**
     * Runs every day at midnight (00:00:00).
     * Cron format: second, minute, hour, day of month, month, day of week
     * You can also use fixedRate = 3600000 (every 1 hour) if you want it more frequent.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void checkAndExpireSubscriptions() {
        log.info("Running scheduled job: Checking for expired photographer subscriptions...");

        LocalDateTime now = LocalDateTime.now();
        List<User> expiredUsers = userRepository.findExpiredPhotographers(
                UserRole.PHOTOGRAPHER,
                SubscriptionStatus.ACTIVE,
                now
        );

        if (expiredUsers.isEmpty()) {
            log.info("No expired subscriptions found.");
            return;
        }

        for (User user : expiredUsers) {
            user.setSubscriptionStatus(SubscriptionStatus.EXPIRED);
            log.info("Photographer subscription expired for user: {}", user.getEmail());
        }

        userRepository.saveAll(expiredUsers);
        log.info("Successfully updated {} expired photographer subscription(s).", expiredUsers.size());
    }
}