package com.jemigraph.jemigraph_backend.listeners;


import com.jemigraph.jemigraph_backend.events.JemigraphEvent;
import com.jemigraph.jemigraph_backend.services.EmailService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(AdminNotificationListener.class);
    private final EmailService emailService;

    @PostConstruct
    public void init() {
        log.info("Notofaction has started !");
    }

    @Async("notificationExecutor")
    @EventListener
    public void handleJemigraphEvent(JemigraphEvent event) {
        emailService.sendEmailToAdmin(event.message());
    }
}
