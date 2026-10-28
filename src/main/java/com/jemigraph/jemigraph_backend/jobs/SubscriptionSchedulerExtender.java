package com.jemigraph.jemigraph_backend.jobs;

import com.jemigraph.jemigraph_backend.Entities.Subscription;
import com.jemigraph.jemigraph_backend.enums.SubscriptionStatus;
import com.jemigraph.jemigraph_backend.repositories.SubscriptionRepository;
import com.jemigraph.jemigraph_backend.services.SmsService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionSchedulerExtender {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private final SubscriptionRepository subscriptionRepository;
  private final SmsService smsService;

  @Scheduled(cron = "0 5 0 * * *", zone = "Africa/Dar_es_Salaam")
  @Transactional
  public void extendExpiredSubscriptions() {

    LocalDateTime now = LocalDateTime.now();

    List<Subscription> expiredSubscriptions =
        subscriptionRepository.findExpiredPhotographerSubscriptions(SubscriptionStatus.ACTIVE, now);

    if (expiredSubscriptions.isEmpty()) {
      log.info("No expired subscriptions found.");
      return;
    }

    for (Subscription subscription : expiredSubscriptions) {

      LocalDateTime newExpiry = subscription.getExpiresAt().plusMonths(1);

      subscription.setExpiresAt(newExpiry);

      String expiryDate = newExpiry.format(DATE_FORMATTER);

      String message =
          "Habari! Jemigraph Tour imekuongezea mwezi mmoja "
              + "wa matumizi ya subscription yako bila malipo. "
              + "Subscription yako mpya inaisha tarehe "
              + expiryDate
              + ". Tafadhali kamilisha profile yako na endelea "
              + "kupokea bookings. Asante kwa kutumia Jemigraph Tour!";

      try {
        if (subscription.getUser().getUserProfile() != null
            && subscription.getUser().getUserProfile().getPhone() != null
            && !subscription.getUser().getUserProfile().getPhone().isBlank()) {
          smsService.sendSms(subscription.getUser().getUserProfile().getPhone(), message);
          log.info("Extension notification sent for subscription ID {}", subscription.getId());

        } else {
          log.warn("No phone number found for subscription ID {}", subscription.getId());
        }

      } catch (Exception e) {
        log.error(
            "Failed to send extension notification for subscription ID {}",
            subscription.getId(),
            e);
      }
    }

    subscriptionRepository.saveAll(expiredSubscriptions);

    log.info("Successfully extended {} subscriptions by one month.", expiredSubscriptions.size());
  }
}
