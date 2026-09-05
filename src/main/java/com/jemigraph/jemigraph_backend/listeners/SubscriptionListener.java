package com.jemigraph.jemigraph_backend.listeners;

import com.jemigraph.jemigraph_backend.DTO.PaymentSuccessEvent;
import com.jemigraph.jemigraph_backend.services.SubscriptionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionListener {
    private final SubscriptionService subscriptionService;
    @EventListener
    @Transactional
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        subscriptionService.activateSubscriptionForUser(event.getUserId(), event.getPlanId());

        System.out.println("Subscription activated automatically via Event for user: " + event.getUserId());
    }
}