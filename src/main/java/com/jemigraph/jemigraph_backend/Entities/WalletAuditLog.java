
package com.jemigraph.jemigraph_backend.Entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallet_audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(nullable = false)
    private BigDecimal preBalance; // Salio lilikuwa kiasi gani kabla?

    @Column(nullable = false)
    private BigDecimal postBalance; // Salio limekuwa kiasi gani baada ya muamala?

    private String description; // Mfano: "Payment received from Order #123"

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}