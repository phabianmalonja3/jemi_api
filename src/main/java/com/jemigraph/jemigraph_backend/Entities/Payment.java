package com.jemigraph.jemigraph_backend.Entities;

import com.jemigraph.jemigraph_backend.enums.SystemPaymentStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

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


    @Column(nullable = false, unique = true)
    private String orderId;


    @Column(unique = true)
    private String transactionNumber;

    @Column(unique = true)
    private String referenceNumber;


    private String receiptNumber;

   
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