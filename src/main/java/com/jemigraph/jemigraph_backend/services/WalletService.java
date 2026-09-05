package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.DTO.TransactionDTO;
import com.jemigraph.jemigraph_backend.DTO.WalletDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface WalletService {
    void lockBookingPayment(UUID photographerId, BigDecimal amount, UUID bookingId);

    void releaseBookingPayment(String bookingId);

    BigDecimal getAvailableBalance(UUID walletId);

   WalletDTO getWalletByOwner(String email);
     List<TransactionDTO> getTransactionHistory(String email);

}