package com.jemigraph.jemigraph_backend.repositories;

import com.jemigraph.jemigraph_backend.Entities.Invoice;
import com.jemigraph.jemigraph_backend.enums.InvoiceStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

  Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

  List<Invoice> findByUserIdOrderByCreatedAtDesc(UUID userId);

  List<Invoice> findByStatusOrderByDueDateAsc(InvoiceStatus status);

  List<Invoice> findByStatusAndDueDateBetween(
      InvoiceStatus status, LocalDateTime start, LocalDateTime end);

  Optional<Invoice> findFirstByUserIdAndStatusOrderByCreatedAtDesc(
      UUID userId, InvoiceStatus status);

  boolean existsByInvoiceNumber(String invoiceNumber);

  Optional<Invoice> findBySubscriptionIdAndStatus(UUID subscriptionId, InvoiceStatus status);
  //  LazyConstant<Object> findBySubscriptionIdAndStatus(UUID id, InvoiceStatus invoiceStatus);
}
