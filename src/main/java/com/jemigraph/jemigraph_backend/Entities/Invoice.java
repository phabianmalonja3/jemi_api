package com.jemigraph.jemigraph_backend.Entities;

import com.jemigraph.jemigraph_backend.enums.InvoiceStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(
    name = "invoices",
    indexes = {
      @Index(name = "idx_invoice_user", columnList = "user_id"),
      @Index(name = "idx_invoice_status", columnList = "status"),
      @Index(name = "idx_invoice_number", columnList = "invoice_number")
    })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "invoice_number", nullable = false, unique = true)
  private String invoiceNumber;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscription_id")
  private Subscription subscription;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, precision = 19, scale = 2)
  @Builder.Default
  private BigDecimal taxAmount = BigDecimal.ZERO;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal totalAmount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private InvoiceStatus status = InvoiceStatus.PENDING;

  @Column(nullable = false)
  private LocalDateTime issuedAt;

  @Column(nullable = false)
  private LocalDateTime dueDate;

  private LocalDateTime paidAt;

  @Column(length = 500)
  private String description;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    LocalDateTime now = LocalDateTime.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
