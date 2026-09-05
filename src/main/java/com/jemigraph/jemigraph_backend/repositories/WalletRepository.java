package com.jemigraph.jemigraph_backend.repositories;
import com.jemigraph.jemigraph_backend.enums.WalletType;
import org.springframework.cloud.gcp.data.spanner.repository.SpannerRepository;
import com.jemigraph.jemigraph_backend.Entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    Optional<Wallet> findByUserId(UUID userId);
    List<Wallet> findByType(WalletType type);

}
