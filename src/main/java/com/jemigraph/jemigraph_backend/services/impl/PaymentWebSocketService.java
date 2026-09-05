package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.DTO.PaymentStatusResponse;
import com.jemigraph.jemigraph_backend.Entities.Payment;
import com.jemigraph.jemigraph_backend.enums.SystemPaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    // ============================================================
    // SEND PAYMENT STATUS
    // ============================================================

    public void sendPaymentStatus(
            Payment payment
    ) {

        if (payment == null) {

            log.warn(
                    "Cannot send payment WebSocket update: payment is null"
            );

            return;
        }

        PaymentStatusResponse response =
                PaymentStatusResponse.builder()
                        .orderId(
                                payment.getOrderId()
                        )
                        .status(
                                payment.getStatus().name()
                        )
                        .message(
                                getPaymentStatusMessage(
                                        payment.getStatus()
                                )
                        )
                        .amount(
                                payment.getAmount()
                        )
                        .planId(
                                payment.getPlanId() != null
                                        ? payment.getPlanId().toString()
                                        : null
                        )
                        .transactionNumber(
                                payment.getTransactionNumber()
                        )
                        .referenceNumber(
                                payment.getReferenceNumber()
                        )
                        .receiptNumber(
                                payment.getReceiptNumber()
                        )
                        .build();

        String destination =
                "/topic/payment/"
                        + payment.getOrderId();

        messagingTemplate.convertAndSend(
                destination,
                response
        );

        log.info(
                "Payment WebSocket update sent. destination={}, orderId={}, status={}",
                destination,
                payment.getOrderId(),
                payment.getStatus()
        );
    }

    // ============================================================
    // STATUS MESSAGE
    // ============================================================

    private String getPaymentStatusMessage(
            SystemPaymentStatus status
    ) {

        if (status == null) {

            return "Payment status unavailable";
        }

        return switch (status) {

            case PENDING ->
                    "Your payment is being processed. Please complete the payment on your phone.";

            case SUCCESS ->
                    "Payment completed successfully. Your subscription is now active.";

            case FAILED ->
                    "Payment failed. Please try again.";

            default ->
                    "Payment status updated.";
        };
    }
}