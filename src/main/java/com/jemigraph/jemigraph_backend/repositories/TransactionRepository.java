package com.jemigraph.jemigraph_backend.repositories;

import com.jemigraph.jemigraph_backend.Entities.Transaction;
import com.jemigraph.jemigraph_backend.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    boolean existsByReferenceId(String referenceId);
    List<Transaction> findByWalletIdOrderByTimestampDesc(UUID walletId);
    @Query("SELECT t FROM Transaction t JOIN FETCH t.wallet w JOIN FETCH w.user u ORDER BY t.createdAt DESC")
    Page<Transaction> findAllTransactions(Pageable pageable);
    Optional<Transaction> findByReferenceId(String refId);



}