package com.jemigraph.jemigraph_backend.Entities;
import com.jemigraph.jemigraph_backend.enums.TransactionStatus;
import com.jemigraph.jemigraph_backend.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType direction; // CREDIT / DEBIT

    @Enumerated(EnumType.STRING)
    private TransactionStatus status; // PENDING, COMPLETED, FAILED

    @Column(unique = true, nullable = false)
    private String referenceId; // Idempotency key (Critical!)

    private String description;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();


    private LocalDateTime timestamp;

}