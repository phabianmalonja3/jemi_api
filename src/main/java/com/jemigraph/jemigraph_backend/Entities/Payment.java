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
      @Index(name = "idx_payment_order_id", columnList = "order_id"),
      @Index(name = "idx_payment_transaction_number", columnList = "transaction_number"),
      @Index(name = "idx_payment_reference_number", columnList = "reference_number"),
      @Index(name = "idx_payment_transaction_id", columnList = "transaction_id"),
      @Index(name = "idx_payment_user_id", columnList = "user_id"),
      @Index(name = "idx_payment_status", columnList = "status")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "order_id", nullable = false, unique = true)
  private String orderId;

  @Column(name = "transaction_number", unique = true)
  private String transactionNumber;

  @Column(name = "reference_number", unique = true)
  private String referenceNumber;

  @Column(name = "receipt_number")
  private String receiptNumber;

  @Column(name = "currency")
  @Builder.Default
  private String currency = "TZS";

  @Column(name = "provider")
  private String provider;

  @Column(name = "transaction_id", unique = true)
  private String transactionId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "plan_id", nullable = false)
  private UUID planId;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Column(name = "phone_number", nullable = false)
  private String phoneNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SystemPaymentStatus status;

  @Column(columnDefinition = "TEXT", name = "gateway_response")
  private String gatewayResponse;

  @Column(columnDefinition = "TEXT", name = "callback_response")
  private String callbackResponse;

  @Column(name = "provider_transaction_id")
  private String providerTransactionId;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "callback_received_at")
  private LocalDateTime callbackReceivedAt;

  @Builder.Default
  @Column(name = "callback_attempts", nullable = false)
  private Integer callbackAttempts = 0;

  @Builder.Default
  @Column(name = "callback_processed", nullable = false)
  private boolean callbackProcessed = false;

  @Column(name = "azam_reference")
  private String azamReference;

  @Column(name = "external_reference")
  private String externalReference;

  @Column(name = "utility_ref")
  private String utilityRef;

  @Column(name = "msisdn")
  private String msisdn;

  @Column(name = "callback_message", columnDefinition = "TEXT")
  private String callbackMessage;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();

    if (status == null) {
      status = SystemPaymentStatus.PENDING;
    }

    if (callbackAttempts == null) {
      callbackAttempts = 0;
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
