package com.jemigraph.jemigraph_backend.enums;

import com.jemigraph.jemigraph_backend.Entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    
    List<Payment> findByUserId(UUID userId);
    
    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findByTransactionNumber(String transactionNumber);

    Optional<Payment> findByReferenceNumber(String referenceNumber);

    boolean existsByTransactionNumber(String transactionNumber);

    Optional<Payment> findFirstByUserIdAndStatusOrderByCreatedAtDesc(UUID id, SystemPaymentStatus systemPaymentStatus);

    List<Payment> findByUserIdAndCreatedAtBetween(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    List<Payment> findByUserIdAndCreatedAtAfter(UUID userId, LocalDateTime startDate);

    List<Payment> findByUserIdAndCreatedAtBefore(UUID userId, LocalDateTime endDate);

    List<Payment> findByUserIdOrderByCreatedAtDesc(UUID userId);
}