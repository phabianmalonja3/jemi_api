package com.jemigraph.jemigraph_backend.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class SystemCommissionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Hibernate itatengeneza UUID
    private UUID id;
    private BigDecimal amount;
    private UUID bookingId;    // Inatoka booking gani?
    private UUID photographerId; // Inatoka kwa nani?
    private LocalDateTime timestamp = LocalDateTime.now();
}