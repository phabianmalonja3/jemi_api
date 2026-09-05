package com.jemigraph.jemigraph_backend.Entities;

import com.jemigraph.jemigraph_backend.enums.WalletType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Builder.Default
    private BigDecimal availableBalance = BigDecimal.ZERO;
    @Column(nullable = true)
    private String pinHash;
    @Column(name = "is_pin_changed")
    @Builder.Default
    boolean isPinChanged = false;
    @Enumerated(EnumType.STRING)
    private WalletType type = WalletType.USER;
    @Builder.Default
    private BigDecimal lockedBalance = BigDecimal.ZERO;
    @Version
    private Long version;
}