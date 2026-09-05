package com.jemigraph.jemigraph_backend.DTO;

import com.jemigraph.jemigraph_backend.enums.SystemPaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {

    private UUID id;
    private String orderId;
    private String transactionNumber;
    private String referenceNumber;
    private String receiptNumber;
    private String provider;
    private UUID userId;
    private UUID planId;
    private BigDecimal amount;
    private String phoneNumber;
    private SystemPaymentStatus status;
    private String providerTransactionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime callbackReceivedAt;
    private Integer callbackAttempts;
    private boolean callbackProcessed;


    }