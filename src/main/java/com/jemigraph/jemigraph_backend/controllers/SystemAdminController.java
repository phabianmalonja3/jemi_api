package com.jemigraph.jemigraph_backend.controllers;

import com.jemigraph.jemigraph_backend.DTO.BookingResponseDTO;
import com.jemigraph.jemigraph_backend.DTO.SubscriberResponseDto;
import com.jemigraph.jemigraph_backend.DTO.TransactionAdminDTO;
import com.jemigraph.jemigraph_backend.DTO.UserDTO;
import com.jemigraph.jemigraph_backend.Entities.Transaction;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.Entities.Wallet;
import com.jemigraph.jemigraph_backend.enums.TransactionType;
import com.jemigraph.jemigraph_backend.enums.UserRole;
import com.jemigraph.jemigraph_backend.enums.WalletType;
import com.jemigraph.jemigraph_backend.repositories.TransactionRepository;
import com.jemigraph.jemigraph_backend.repositories.WalletRepository;
import com.jemigraph.jemigraph_backend.services.AdminService;
import com.jemigraph.jemigraph_backend.services.BookingService;
import com.jemigraph.jemigraph_backend.services.PaymentSystemService;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class SystemAdminController {
    private final AdminService adminService;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
private final BookingService bookingService;
private final PaymentSystemService paymentSystemService;

    @PatchMapping("/suspend-account/{userId}")
    public ResponseEntity<?> suspendAccount(@PathVariable UUID userId) {
        try {
            User updatedUser = adminService.accountSuspension(userId);
            return ResponseEntity.ok(updatedUser); // Return user object, sio string
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }

    }
    @DeleteMapping("/remove-account/{userId}")
    public ResponseEntity<?> deleteAccount(@PathVariable UUID userId) {
        try {
            return ResponseEntity.ok().build(); // Return user object, sio string
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }

    }
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(required = false) UserRole role
            ) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(order), sort));
        return ResponseEntity.ok(adminService.findFilteredUsers(name, role, pageable));
    }
    @GetMapping("/system-balance")
    public ResponseEntity<?> getBalance(Principal principal) {
        return ResponseEntity.ok(adminService.getSystemBalance());
    }
    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionAdminDTO>> getAllTransactions(
            @PageableDefault(size = 10, sort = "createdAt",direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TransactionAdminDTO> transactions = adminService.getAllTransactionsForAdmin(pageable);
        return ResponseEntity.ok(transactions);
    }
    @GetMapping("/transactions/recent")
    public ResponseEntity<List<TransactionAdminDTO>> getRecentTransactions() {
        Pageable topFive = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        return ResponseEntity.ok(adminService.getRecentTransactions(topFive));
    }

    @PostMapping("/verify-photographer/{userId}")
    public ResponseEntity<?> verifyPhotographer(@PathVariable UUID userId) {
        adminService.verifyPhotographer(userId);
        return ResponseEntity.ok("Photographer verified successfully!");
    }


    @GetMapping("/users/pending-verification")
    public ResponseEntity<List<UserDTO>> getPendingUsers() {
        return ResponseEntity.ok(adminService.getUnverifiedPhotographers());
    }



    @GetMapping("/bookings")
    public ResponseEntity<Page<BookingResponseDTO>> getAllBookings(Pageable pageable) {
        Page<BookingResponseDTO> bookings = bookingService.getAllBookings(pageable);
        return ResponseEntity.ok(bookings);
    }


    @GetMapping("/subscribers")
    public ResponseEntity<List<SubscriberResponseDto>> getAllSubscribers() {
        List<SubscriberResponseDto> subscribers = paymentSystemService.getAllSubscribers();
        return ResponseEntity.ok(subscribers);
    }
}
