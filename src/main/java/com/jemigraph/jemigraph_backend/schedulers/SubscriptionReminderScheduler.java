package com.jemigraph.jemigraph_backend.schedulers;

import com.jemigraph.jemigraph_backend.Entities.Subscription;
import com.jemigraph.jemigraph_backend.enums.SubscriptionStatus;
import com.jemigraph.jemigraph_backend.repositories.SubscriptionRepository;
import com.jemigraph.jemigraph_backend.services.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionReminderScheduler {

	private final SubscriptionRepository subscriptionRepository;
	private final NotificationService notificationService;

	@Scheduled(cron = "0 0 9 * * *")
	public void sendThreeDayReminders() {

		LocalDateTime now = LocalDateTime.now();

		LocalDateTime targetDate = now.plusDays(3);

		LocalDateTime start = targetDate
				.withHour(0)
				.withMinute(0)
				.withSecond(0)
				.withNano(0);

		LocalDateTime end = targetDate
				.withHour(23)
				.withMinute(59)
				.withSecond(59)
				.withNano(999_999_999);

		List<Subscription> subscriptions =
				subscriptionRepository.findSubscriptionsExpiringBetween(
						SubscriptionStatus.ACTIVE,
						start,
						end
				);
		for (Subscription subscription : subscriptions) {

//			notificationService.sendSubscriptionExpiryReminder(
//					subscription
//			);

			log.info(
					"3-day subscription reminder sent to user {}",
					subscription.getUser().getEmail()
			);
		}
	}
}
