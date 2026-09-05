package com.jemigraph.jemigraph_backend.listeners;

import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.events.PhotographerVerifiedEvent;
import com.jemigraph.jemigraph_backend.services.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PhotographerEventListener {
    private final SmsService smsService;


    @Async
    @EventListener
    public void handlePhotographerVerified(PhotographerVerifiedEvent event) {
        User verifiedUser = event.user();

        if (verifiedUser.getUserProfile() != null && verifiedUser.getUserProfile().getPhone() != null) {
            String smsMessage = getString(verifiedUser);

            try {
                smsService.sendSms(verifiedUser.getUserProfile().getPhone(), smsMessage);
            } catch (Exception e) {
               log.info("Failed to send verification SMS: " + e.getMessage());
                System.err.println("Failed to send verification SMS: " + e.getMessage());
            }
        }
    }

    private static @NonNull String getString(User verifiedUser) {
        String name = verifiedUser.getName() != null ? verifiedUser.getName().toUpperCase() : "ESTEEMED PARTNER";
        String email = verifiedUser.getEmail() != null ? verifiedUser.getEmail() : "N/A";

        return String.format(
                "Congratulations %s! Your Jemigraph Tour account has been successfully verified. You may now sign in using your email as your username (%s). Thank you for joining our network—we are excited to work with you.",
                name,
                email
        );
    }
}
