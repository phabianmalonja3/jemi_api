package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.Entities.Invoice;
import com.jemigraph.jemigraph_backend.Entities.Subscription;
import com.jemigraph.jemigraph_backend.Entities.SubscriptionPlan;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.enums.InvoiceStatus;
import com.jemigraph.jemigraph_backend.repositories.InvoiceRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceService {

  private final InvoiceRepository invoiceRepository;
  private final InvoicePdfService invoicePdfService;

  /** Create invoice for a new subscription/payment. */
  public Invoice createInvoice(User user, SubscriptionPlan plan) {

    BigDecimal amount = plan.getPrice();

    BigDecimal taxAmount = BigDecimal.ZERO;

    BigDecimal totalAmount = amount.add(taxAmount);

    LocalDateTime now = LocalDateTime.now();

    Invoice invoice =
        Invoice.builder()
            .invoiceNumber(generate())
            .user(user)
            .amount(amount)
            .taxAmount(taxAmount)
            .totalAmount(totalAmount)
            .status(InvoiceStatus.PENDING)
            .issuedAt(now)
            .dueDate(now.plusDays(3))
            .description("Subscription - " + plan.getName().name())
            .build();

    return invoiceRepository.save(invoice);
  }

  /**
   * Create renewal invoice for an existing subscription.
   *
   * <p>The invoice is due when the current subscription expires.
   */
  public Invoice createRenewalInvoice(Subscription subscription) {

    User user = subscription.getUser();

    SubscriptionPlan plan = subscription.getSubscriptionPackage();

    BigDecimal amount = plan.getPrice();

    BigDecimal taxAmount = BigDecimal.ZERO;

    BigDecimal totalAmount = amount.add(taxAmount);

    LocalDateTime now = LocalDateTime.now();

    Invoice invoice =
        Invoice.builder()
            .invoiceNumber(generate())
            .user(user)
            .subscription(subscription)
            .amount(amount)
            .taxAmount(taxAmount)
            .totalAmount(totalAmount)
            .status(InvoiceStatus.PENDING)
            .issuedAt(now)
            .dueDate(subscription.getExpiresAt())
            .description("Subscription Renewal - " + plan.getName().name())
            .build();

    return invoiceRepository.save(invoice);
  }

  @Transactional(readOnly = true)
  public Invoice getById(UUID id) {

    return invoiceRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Invoice not found: " + id));
  }

  @Transactional(readOnly = true)
  public Invoice getByInvoiceNumber(String invoiceNumber) {

    return invoiceRepository
        .findByInvoiceNumber(invoiceNumber)
        .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceNumber));
  }

  @Transactional(readOnly = true)
  public List<Invoice> getUserInvoices(UUID userId) {

    return invoiceRepository.findByUserIdOrderByCreatedAtDesc(userId);
  }

  public Invoice markAsPaid(Invoice invoice) {

    invoice.setStatus(InvoiceStatus.PAID);

    invoice.setPaidAt(LocalDateTime.now());

    return invoiceRepository.save(invoice);
  }

  public Invoice cancel(Invoice invoice) {

    invoice.setStatus(InvoiceStatus.CANCELLED);

    return invoiceRepository.save(invoice);
  }

  /** Generate invoice PDF. */
  @Transactional(readOnly = true)
  public byte[] generatePdf(UUID invoiceId) {

    Invoice invoice =
        invoiceRepository
            .findById(invoiceId)
            .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId));

    return invoicePdfService.generateInvoicePdf(invoice);
  }

  private String generate() {

    String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

    String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

    return "INV-" + date + "-" + random;
  }
}
