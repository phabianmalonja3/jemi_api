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
public class PaymentStatusResponse {

    private String orderId;

    private String status;

    private String message;

    private BigDecimal amount;

    private String planId;

    private String transactionNumber;

    private String referenceNumber;

    private String receiptNumber;
}