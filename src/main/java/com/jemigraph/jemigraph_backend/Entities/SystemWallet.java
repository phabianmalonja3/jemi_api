package com.jemigraph.jemigraph_backend.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
public class SystemWallet {

    @Id
    private UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001"); // Fixed UUID

    private BigDecimal totalCommissionCollected = BigDecimal.ZERO;
    private BigDecimal availableBalance = BigDecimal.ZERO;
}