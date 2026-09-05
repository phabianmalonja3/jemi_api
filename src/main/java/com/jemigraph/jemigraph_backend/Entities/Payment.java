package com.jemigraph.jemigraph_backend.Entities;

import com.jemigraph.jemigraph_backend.enums.SystemPaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "system_payments",
        indexes = {
                @Index(name = "idx_payment_order_id", columnList = "orderId"),
                @Index(name = "idx_payment_transaction_number", columnList = "transactionNumber"),
                @Index(name = "idx_payment_reference_number", columnList = "referenceNumber"),
                @Index(name = "idx_payment_user_id", columnList = "userId"),
                @Index(name = "idx_payment_status", columnList = "status")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Internal Jemigraph order ID.
     *
     * Example:
     * JEMI-4B0B29D051884443
     */
    @Column(nullable = false, unique = true)
    private String orderId;

    /**
     * CashPay transaction number.
     *
     * Example:
     * JEMI-4B0B29D051884443
     *
     * This is the value sent to:
     * /generate
     * /ussdpush
     * and returned in callback.
     */
    @Column(unique = true)
    private String transactionNumber;

    /**
     * CashPay generated reference number.
     *
     * Example:
     * 50123456
     */
    @Column(unique = true)
    private String referenceNumber;

    /**
     * Receipt number returned by mobile money provider
     * after successful payment.
     */
    private String receiptNumber;

    /**
     * Provider used for payment.
     *
     * Example:
     * M-PESA
     * TigoPesa
     * AirtelMoney
     */
    private String provider;

    /**
     * User who is making payment.
     */
    @Column(nullable = false)
    private UUID userId;

    /**
     * Subscription plan being purchased.
     */
    @Column(nullable = false)
    private UUID planId;

    /**
     * Amount in TZS.
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /**
     * Customer phone number.
     *
     * Stored in international format:
     * 2557XXXXXXXX
     */
    @Column(nullable = false)
    private String phoneNumber;

    /**
     * Payment status.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SystemPaymentStatus status;

    /**
     * Complete gateway response.
     *
     * Useful for debugging/audit.
     */
    @Column(columnDefinition = "TEXT")
    private String gatewayResponse;

    /**
     * CashPay callback response.
     */
    @Column(columnDefinition = "TEXT")
    private String callbackResponse;

    /**
     * Provider receipt / transaction number.
     */
    private String providerTransactionId;

    /**
     * When payment was created.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * When payment was last updated.
     */
    private LocalDateTime updatedAt;

    /**
     * When CashPay callback was received.
     */
    private LocalDateTime callbackReceivedAt;

    /**
     * Number of callback attempts received.
     */
    @Builder.Default
    @Column(nullable = false)
    private Integer callbackAttempts = 0;

    /**
     * Prevent processing the same successful callback twice.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean callbackProcessed = false;

    @PrePersist
    protected void onCreate() {

        createdAt = LocalDateTime.now();

        if (status == null) {
            status = SystemPaymentStatus.PENDING;
        }

        if (callbackAttempts == null) {
            callbackAttempts = 0;
        }

        if (!callbackProcessed) {
            callbackProcessed = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}