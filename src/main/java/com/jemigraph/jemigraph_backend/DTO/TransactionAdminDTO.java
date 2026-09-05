package com.jemigraph.jemigraph_backend.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransactionAdminDTO {
    private UUID id;
    private String userName; // Tunataka jina la user
    private BigDecimal amount;
    private String type;
    private String status;
    private String referenceId;
    private LocalDateTime createdAt;
}
