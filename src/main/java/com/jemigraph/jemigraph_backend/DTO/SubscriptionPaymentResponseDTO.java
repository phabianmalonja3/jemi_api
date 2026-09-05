package com.jemigraph.jemigraph_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPaymentResponseDTO {
    private String orderId;
    private String status;
    private BigDecimal amount;
    private String planId;
    private String transactionNumber;
    private String referenceNumber;
    private String receiptNumber;
    private String phoneNumber;
    private String planName;
    private String planDescription;
    private Integer durationInDays;
    private String startDate;
    private String endDate;
    private boolean subscriptionActive;
    private String subscriptionStatus;
}