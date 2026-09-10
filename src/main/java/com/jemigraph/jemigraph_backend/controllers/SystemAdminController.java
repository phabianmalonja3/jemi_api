package com.jemigraph.jemigraph_backend.controllers;

import com.jemigraph.jemigraph_backend.DTO.BookingResponseDTO;
import com.jemigraph.jemigraph_backend.DTO.SubscriberResponseDto;
import com.jemigraph.jemigraph_backend.DTO.TransactionAdminDTO;
import com.jemigraph.jemigraph_backend.DTO.UserDTO;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.enums.UserRole;
import com.jemigraph.jemigraph_backend.repositories.TransactionRepository;
import com.jemigraph.jemigraph_backend.repositories.WalletRepository;
import com.jemigraph.jemigraph_backend.services.AdminService;
import com.jemigraph.jemigraph_backend.services.BookingService;
import com.jemigraph.jemigraph_backend.services.PaymentSystemService;
import com.jemigraph.jemigraph_backend.services.SessionService;
import java.security.Principal;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class SystemAdminController {
    private final AdminService adminService;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final BookingService bookingService;
    private final PaymentSystemService paymentSystemService;
    private final SessionService sessionService;

    @PatchMapping("/suspend-account/{userId}")
    public ResponseEntity<?> suspendAccount(@PathVariable UUID userId) {
        try {
            User updatedUser = adminService.accountSuspension(userId);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }

    }
    @DeleteMapping("/remove-account/{userId}")
    public ResponseEntity<?> deleteAccount(@PathVariable UUID userId) {
        try {
            return ResponseEntity.ok().build();
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

   
    /** Get all active sessions mapped by user ID. */
    @GetMapping("/sessions")
    public ResponseEntity<Map<UUID, Map<Object, Object>>> getAllActiveSessions() {
        return ResponseEntity.ok(sessionService.getAllActiveSessions());
    }

    /** Get a list of all user IDs with active sessions. */
    @GetMapping("/sessions/users")
    public ResponseEntity<List<UUID>> getActiveUsers() {
        return ResponseEntity.ok(sessionService.getActiveUsers());
    }

    /** Get session information using email or username identifier. */
    @GetMapping("/sessions/identifier/{identifier}")
    public ResponseEntity<Map<Object, Object>> getSessionByIdentifier(@PathVariable String identifier) {
        Map<Object, Object> session = sessionService.getSessionByIdentifier(identifier);
        if (session.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(session);
    }

    /** Clear a specific user's session using their UUID. */
    @DeleteMapping("/sessions/user/{userId}")
    public ResponseEntity<Void> clearUserSession(@PathVariable UUID userId) {
        sessionService.clearUserSession(userId);
        return ResponseEntity.noContent().build();
    }

    /** Clear a session using email or username identifier. */
    @DeleteMapping("/sessions/identifier/{identifier}")
    public ResponseEntity<Void> clearSessionByIdentifier(@PathVariable String identifier) {
        sessionService.clearSessionByIdentifier(identifier);
        return ResponseEntity.noContent().build();
    }
}