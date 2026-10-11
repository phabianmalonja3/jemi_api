package com.jemigraph.jemigraph_backend.schedulers;

import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.Entities.UserProfile;
import com.jemigraph.jemigraph_backend.enums.UserRole;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.services.SmsService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PhotographerProfileReminderScheduler {

  private static final ZoneId TANZANIA_ZONE = ZoneId.of("Africa/Dar_es_Salaam");

  private static final int REMINDER_INTERVAL_DAYS = 3;

  private final UserRepository userRepository;
  private final SmsService smsService;

  @Transactional
  @Scheduled(cron = "0 0 3 * * *", zone = "Africa/Dar_es_Salaam")
  public void sendProfileReminders() {

    LocalDateTime now = LocalDateTime.now(TANZANIA_ZONE);

    LocalDateTime reminderBefore = now.minusDays(REMINDER_INTERVAL_DAYS);

    List<User> photographers =
        userRepository.findPhotographersNeedingProfileReminder(
            UserRole.PHOTOGRAPHER, reminderBefore);

    log.info("Found {} photographers needing profile reminders", photographers.size());

    for (User photographer : photographers) {
      try {
        sendReminder(photographer, now);
      } catch (Exception e) {
        log.error("Failed to send profile reminder to photographer {}", photographer.getId(), e);
      }
    }
  }

  private void sendReminder(User photographer, LocalDateTime now) {

    UserProfile profile = photographer.getUserProfile();

    if (profile == null || profile.getPhone() == null || profile.getPhone().isBlank()) {

      log.warn("Skipping photographer {}: profile or phone is missing", photographer.getId());
      return;
    }

    boolean hasPackages =
        photographer.getPackages() != null && !photographer.getPackages().isEmpty();

    boolean hasLocation =
        photographer.getLocation() != null
            && photographer.getLocation().getLatitude() != null
            && photographer.getLocation().getLongitude() != null;

    if (hasPackages && hasLocation) {
      return;
    }

    String message;

    if (!hasPackages && !hasLocation) {
      message =
          "Habari "
              + profile.getDisplayName()
              + "! Kamilisha profile yako ya Jemigrapher kwa "
              + "kuweka packages zako na location ya ramani. "
              + "Hii itawasaidia wateja kuona huduma zako "
              + "na kufanya booking. Fungua Jemigrapher leo.";
    } else if (!hasPackages) {
      message =
          "Habari "
              + profile.getDisplayName()
              + "! Bado hujaweka packages zako kwenye "
              + "Jemigrapher. Weka huduma na bei zako ili "
              + "wateja waweze kuziona na kufanya booking.";
    } else {
      message =
          "Habari "
              + profile.getDisplayName()
              + "! Bado hujaweka location yako kwenye "
              + "ramani ya Jemigrapher. Weka location ili "
              + "wateja waweze kukupata na kufanya booking.";
    }

    smsService.sendSms(profile.getPhone(), message);

    photographer.setLastProfileReminderAt(now);
    userRepository.save(photographer);

    log.info("Profile reminder sent to photographer {}", photographer.getId());
  }
}
