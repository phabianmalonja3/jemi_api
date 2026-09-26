package com.jemigraph.jemigraph_backend.DTO;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentInitiationResponse {
    private String orderId;
    private String status; 
    private String message;
    private String phoneNumber;
    private BigDecimal amount;
}