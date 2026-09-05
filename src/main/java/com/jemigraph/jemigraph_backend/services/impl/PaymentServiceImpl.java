package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.Entities.Bookings;
import com.jemigraph.jemigraph_backend.Entities.Transaction;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.Entities.Wallet;
import com.jemigraph.jemigraph_backend.enums.PaymentStatus;
import com.jemigraph.jemigraph_backend.enums.TransactionStatus;
import com.jemigraph.jemigraph_backend.enums.TransactionType;
import com.jemigraph.jemigraph_backend.enums.WalletType;
import com.jemigraph.jemigraph_backend.events.PaymentEvent;
import com.jemigraph.jemigraph_backend.repositories.BookingRepository;
import com.jemigraph.jemigraph_backend.repositories.TransactionRepository;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.repositories.WalletRepository;
import com.jemigraph.jemigraph_backend.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final WalletRepository walletRepository;
    private final BookingRepository bookingRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    private final ApplicationEventPublisher applicationEventPublisher;
   @Override
    @Transactional
    public void updatePaymentStatus(UUID bookingId, BigDecimal paymentAmount) {
        Bookings booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found!"));
        BigDecimal totalAmount = BigDecimal.valueOf(booking.getPkg().getPrice());
        BigDecimal fortyPercent = totalAmount.multiply(new BigDecimal("0.40")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal sixtyPercent = totalAmount.multiply(new BigDecimal("0.60")).setScale(2, RoundingMode.HALF_UP);
        if (booking.getPaymentStatus() == PaymentStatus.FULLY_PAID) {
            throw new RuntimeException("Booking is already fully paid.");
        }

        if (booking.getPaymentStatus() == null || booking.getPaymentStatus() == PaymentStatus.UNPAID) {
            if (paymentAmount.compareTo(fortyPercent) != 0) {
                throw new RuntimeException("Initial payment must be 40% (" + fortyPercent + ")");
            }
            booking.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
        } else if (booking.getPaymentStatus() == PaymentStatus.PARTIALLY_PAID) {
            if (paymentAmount.compareTo(sixtyPercent) != 0) {
                throw new RuntimeException("Final payment must be 60% (" + sixtyPercent + ")");
            }
            booking.setPaymentStatus(PaymentStatus.FULLY_PAID);
        }
       booking.setAmountPaid(booking.getAmountPaid().add(paymentAmount));
        bookingRepository.save(booking);

        Wallet photographerWallet = walletRepository.findByUserId(booking.getPhotographer().getId())
                .orElseThrow(() -> new RuntimeException("Photographer wallet not found!"));

        photographerWallet.setLockedBalance(photographerWallet.getLockedBalance().add(paymentAmount));
        walletRepository.save(photographerWallet);


        transactionRepository.save(Transaction.builder()
                .wallet(photographerWallet)
                .amount(paymentAmount)
                .direction(TransactionType.CREDIT)
                .status(TransactionStatus.COMPLETED)
                .referenceId("TXN_" + UUID.randomUUID())
                .description("Payment received for Booking: " + bookingId)
                .build());


        if (booking.getPaymentStatus() == PaymentStatus.FULLY_PAID) {
            BigDecimal totalLocked = photographerWallet.getLockedBalance(); // 100% ya bei
            BigDecimal commissionRate = new BigDecimal("0.1");

            BigDecimal platformFee = totalLocked.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);

            BigDecimal netAmount = totalLocked.subtract(platformFee);
            photographerWallet.setLockedBalance(BigDecimal.ZERO);
            photographerWallet.setAvailableBalance(photographerWallet.getAvailableBalance().add(netAmount));
            walletRepository.save(photographerWallet);

            Wallet systemWallet = walletRepository.findByType(WalletType.SYSTEM)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("System wallet not found!"));
            systemWallet.setAvailableBalance(systemWallet.getAvailableBalance().add(platformFee));
            walletRepository.save(systemWallet);

            // Log ya Commission
            transactionRepository.save(Transaction.builder()
                    .wallet(systemWallet)
                    .amount(platformFee)
                    .direction(TransactionType.CREDIT)
                    .status(TransactionStatus.COMPLETED)
                    .referenceId("COMM_" + UUID.randomUUID())
                    .description("Commission from Booking: " + bookingId)
                    .build());
        }

        applicationEventPublisher.publishEvent(new PaymentEvent(
                booking.getId(),
                booking.getPhotographer().getId(),
                paymentAmount,
                booking.getPaymentStatus()
        ));
   }

    @Override
    @Transactional
    public void withdrawFunds(BigDecimal amount ,String phoneNumber ,String email) {


        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new IllegalArgumentException("Phone number is required.");
        }
        String referenceId = "TXN_"+UUID.randomUUID();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Wallet not found!"));
        if (wallet.getAvailableBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient available balance.");
        }
        wallet.setAvailableBalance(wallet.getAvailableBalance().subtract(amount));
        walletRepository.save(wallet);
        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(amount)
                .direction(TransactionType.DEBIT)
                .status(TransactionStatus.COMPLETED)
                .referenceId(referenceId)
                .description("Withdrawal to: " + phoneNumber)
                .build();
        transactionRepository.save(transaction);
    }
}