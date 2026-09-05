package com.jemigraph.jemigraph_backend.repositories;

import com.jemigraph.jemigraph_backend.Entities.WalletAuditLog;
import org.springframework.cloud.gcp.data.spanner.repository.SpannerRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WalletAuditLogRepository extends JpaRepository<WalletAuditLog, UUID> {
    List<WalletAuditLog> findByTransaction_Wallet_IdOrderByTimestampDesc(UUID walletId);
}