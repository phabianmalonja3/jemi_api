package com.jemigraph.jemigraph_backend.DTO;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionDTO {
    private BigDecimal amount;
    private String direction; // DEBIT au CREDIT
    private String status;
    private String description;
    private LocalDateTime timestamp;
    private String referenceId;
}