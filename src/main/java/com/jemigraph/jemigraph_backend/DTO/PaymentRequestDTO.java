package com.jemigraph.jemigraph_backend.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequestDTO {
    private BigDecimal amount;
}
