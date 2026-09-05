package com.jemigraph.jemigraph_backend.DTO;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentInitiationResponse {
    private String orderId;
    private String status; // "PENDING"
    private String message;
    private String phoneNumber;
    private BigDecimal amount;
}