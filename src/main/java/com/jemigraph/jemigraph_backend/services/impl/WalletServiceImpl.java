package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.DTO.TransactionDTO;
import com.jemigraph.jemigraph_backend.DTO.WalletDTO;
import com.jemigraph.jemigraph_backend.Entities.*;

import com.jemigraph.jemigraph_backend.enums.TransactionStatus;
import com.jemigraph.jemigraph_backend.enums.TransactionType;
import com.jemigraph.jemigraph_backend.repositories.TransactionRepository;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.repositories.WalletAuditLogRepository;
import com.jemigraph.jemigraph_backend.repositories.WalletRepository;
import com.jemigraph.jemigraph_backend.services.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final WalletAuditLogRepository auditRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void lockBookingPayment(UUID photographerId, BigDecimal amount, UUID bookingId) {
        String refId = "BOOKING-" + bookingId;
        if (transactionRepository.existsByReferenceId(refId)) {
            throw new IllegalStateException("Muamala huu umeshapokelewa!");
        }

        Wallet wallet = walletRepository.findByUserId(photographerId)
                .orElseThrow(() -> new RuntimeException("Wallet haijapatikana"));

        // Update Locked Balance
        wallet.setLockedBalance(wallet.getLockedBalance().add(amount));
        walletRepository.save(wallet);

        // Record Transaction
        Transaction tx = Transaction.builder()
                .wallet(wallet)
                .amount(amount)
                .direction(TransactionType.CREDIT)
                .status(TransactionStatus.PENDING)
                .referenceId(refId)
                .description("Locking funds for booking: " + bookingId)
                .build();
        transactionRepository.save(tx);
    }

    @Override
    @Transactional
    public void releaseBookingPayment(String bookingId) {
        String refId = "BOOKING-" + bookingId;

        // Tafuta transaction ya awali ya PENDING
        Transaction pendingTx = transactionRepository.findByReferenceId(refId)
                .orElseThrow(() -> new RuntimeException("Transaction haijapatikana"));

        if (pendingTx.getStatus() == TransactionStatus.COMPLETED) {
            throw new IllegalStateException("Booking hii imeshakamilishwa tayari!");
        }

        Wallet wallet = pendingTx.getWallet();
        BigDecimal amount = pendingTx.getAmount();

        // 1. Toa kwenye Locked
        wallet.setLockedBalance(wallet.getLockedBalance().subtract(amount));
        // 2. Ingiza kwenye Available
        wallet.setAvailableBalance(wallet.getAvailableBalance().add(amount));
        walletRepository.save(wallet);

        // 3. Update status ya transaction
        pendingTx.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(pendingTx);
    }

    @Override
    public BigDecimal getAvailableBalance(UUID walletId) {
        return walletRepository.findById(walletId)
                .map(Wallet::getAvailableBalance)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public WalletDTO getWalletByOwner(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found!"));

        return WalletDTO.builder()
                .availableBalance(wallet.getAvailableBalance() != null ? wallet.getAvailableBalance() : BigDecimal.ZERO)
                .lockedBalance(wallet.getLockedBalance() != null ? wallet.getLockedBalance() : BigDecimal.ZERO)
                .currency("TZS")
                .build();

    }

    @Override
    public List<TransactionDTO> getTransactionHistory(String email) {

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found!"));

            Wallet wallet = walletRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Wallet not found!"));

            // Tunatafuta transactions zote za wallet hii
            return transactionRepository.findByWalletIdOrderByTimestampDesc(wallet.getId())
                    .stream()
                    .map(t -> TransactionDTO.builder()
                            .amount(t.getAmount())
                            .direction(t.getDirection().name())
                            .status(t.getStatus().name())
                            .description(t.getDescription())
                            .timestamp(t.getCreatedAt()) // Hakikisha una field ya muda
                            .referenceId(t.getReferenceId())
                            .build())
                    .collect(Collectors.toList());

    }
}