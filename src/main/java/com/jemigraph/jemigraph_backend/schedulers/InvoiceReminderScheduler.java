package com.jemigraph.jemigraph_backend.schedulers;

import com.jemigraph.jemigraph_backend.Entities.Invoice;
import com.jemigraph.jemigraph_backend.Entities.Subscription;
import com.jemigraph.jemigraph_backend.enums.InvoiceStatus;
import com.jemigraph.jemigraph_backend.enums.SubscriptionStatus;
import com.jemigraph.jemigraph_backend.repositories.InvoiceRepository;
import com.jemigraph.jemigraph_backend.repositories.SubscriptionRepository;
import com.jemigraph.jemigraph_backend.services.EmailService;
import com.jemigraph.jemigraph_backend.services.InvoiceService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceReminderScheduler {

  private final SubscriptionRepository subscriptionRepository;
  private final InvoiceRepository invoiceRepository;
  private final InvoiceService invoiceService;
  private final EmailService emailService;

  @Scheduled(cron = "0 0 9 * * *")
  public void createAndSendRenewalInvoices() {

    LocalDateTime now = LocalDateTime.now();

    LocalDateTime from = now.plusDays(3).withHour(0).withMinute(0).withSecond(0);
    LocalDateTime to = now.plusDays(3).withHour(23).withMinute(59).withSecond(59);

    List<Subscription> subscriptions =
            subscriptionRepository.findByStatusAndExpiresAtBetween(
                    SubscriptionStatus.ACTIVE,
                    from,
                    to);

    for (Subscription subscription : subscriptions) {

      try {

        boolean exists =
                invoiceRepository
                        .findBySubscriptionIdAndStatus(
                                subscription.getId(),
                                InvoiceStatus.PENDING)
                        .isPresent();

        if (exists) {
          log.info(
                  "Pending renewal invoice already exists for subscription {}",
                  subscription.getId());
          continue;
        }

        Invoice invoice =
                invoiceService.createRenewalInvoice(subscription);

        emailService.sendInvoice(
                subscription.getUser(),
                invoice);

        log.info(
                "Renewal invoice {} created and sent to {}",
                invoice.getInvoiceNumber(),
                subscription.getUser().getEmail());

      } catch (Exception e) {

        log.error(
                "Failed to process renewal invoice for subscription {}",
                subscription.getId(),
                e);
      }
    }
  }
}
