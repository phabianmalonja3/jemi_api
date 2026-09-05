package com.jemigraph.jemigraph_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.UUID;

@Data
@AllArgsConstructor
public class PaymentSuccessEvent {
    private UUID userId;
    private UUID planId;
    private String transactionId;
}