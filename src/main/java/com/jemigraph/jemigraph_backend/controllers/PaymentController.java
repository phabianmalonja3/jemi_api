package com.jemigraph.jemigraph_backend.controllers;

import com.jemigraph.jemigraph_backend.DTO.*;
import com.jemigraph.jemigraph_backend.Entities.Payment;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.enums.PaymentRepository;
import com.jemigraph.jemigraph_backend.repositories.SubscriptionPlanRepository;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.services.PaymentSystemService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
  private final PaymentSystemService paymentService;
  private final UserRepository userRepository;
  private final PaymentRepository paymentRepository;
  private final SubscriptionPlanRepository subscriptionPlanRepository;

  @GetMapping("/my-payments")
  public ResponseEntity<List<Payment>> getMyPayments(Principal principal) {

    User user =
        userRepository
            .findByEmail(principal.getName())
            .orElseThrow(() -> new RuntimeException("User not found!"));

    List<Payment> payments = paymentRepository.findByUserId(user.getId());

    return ResponseEntity.ok(payments);
  }

  @GetMapping("/all")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<Payment>> getAllPayments() {

    List<Payment> payments = paymentRepository.findAll();

    return ResponseEntity.ok(payments);
  }

  @PostMapping("/subscription/pay")
  public ResponseEntity<PaymentInitiationResponse> initiatePayment(
      @RequestParam String email, @RequestParam UUID planId, @RequestParam String phoneNumber) {

    User currentUser =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

    PaymentInitiationResponse response =
        paymentService.initiatePaymentAsync(currentUser, planId, phoneNumber);

    return ResponseEntity.ok(response);
  }

  @PostMapping("/pay")
  public ResponseEntity<PaymentInitiationResponse> initiatePayment(
      Principal principal, @Valid @RequestParam PaymentInitRequestDTO paymentInitRequestDTO) {

    User currentUser =
        userRepository
            .findByEmail(principal.getName())
            .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

    PaymentInitiationResponse response =
        paymentService.initiatePaymentAsync(
            currentUser, paymentInitRequestDTO.getPlanId(), paymentInitRequestDTO.getPhoneNumber());

    return ResponseEntity.ok(response);
  }

  @GetMapping("/status/{orderId}")
  public ResponseEntity<SubscriptionPaymentResponseDTO> getPaymentStatus(
      @PathVariable String orderId) {
    SubscriptionPaymentResponseDTO response = paymentService.getPaymentStatusResponse(orderId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/callback")
  public ResponseEntity<Map<String, String>> paymentCallback(
      @RequestBody PaymentCallbackDto callbackPayload) {

    boolean processed = paymentService.handleCallback(callbackPayload);

    if (processed) {

      return ResponseEntity.ok(
          Map.of("status", "received", "message", "Callback processed successfully"));
    }

    return ResponseEntity.badRequest()
        .body(Map.of("status", "failed", "message", "Invalid or failed payment callback"));
  }

  @GetMapping("/{orderId}/receipt")
  public ResponseEntity<ByteArrayResource> downloadReceipt(@PathVariable String orderId) {
    try {
      byte[] pdfBytes = paymentService.generateReceiptPdf(orderId);
      String fileName = "receipt_" + orderId + ".pdf";

      ByteArrayResource resource = new ByteArrayResource(pdfBytes);

      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
      headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
      headers.add(HttpHeaders.PRAGMA, "no-cache");
      headers.add(HttpHeaders.EXPIRES, "0");

      return ResponseEntity.ok()
          .headers(headers)
          .contentLength(pdfBytes.length)
          .contentType(MediaType.APPLICATION_PDF)
          .body(resource);

    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<Payment> getPaymentById(@PathVariable UUID id, Principal principal) {
    Payment payment = paymentService.getPaymentByIdAndUser(id);
    return ResponseEntity.ok(payment);
  }

  @GetMapping("/download-all")
  public ResponseEntity<ByteArrayResource> downloadAllPayments(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endDate,
      java.security.Principal principal) {

    try {
      if (principal == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
      }

      // Tafuta user kupitia email/username iliyopo kwenye Token/Principal
      com.jemigraph.jemigraph_backend.Entities.User user =
          userRepository
              .findByEmail(principal.getName())
              .orElseThrow(() -> new RuntimeException("User not found: " + principal.getName()));

      UUID userId = user.getId();

      LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
      LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : null;
      List<Payment> payments = paymentService.getPaymentsByUserAndDateRange(userId, start, end);

      if (payments.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      }

      // Generate bulk PDF report
      byte[] pdfBytes = paymentService.generateBulkReceiptPdf(payments, user);

      String dateRange = "";
      if (startDate != null && endDate != null) {
        dateRange = "_" + startDate + "_to_" + endDate;
      } else if (startDate != null) {
        dateRange = "_from_" + startDate;
      }

      String fileName = "payments_report" + dateRange + ".pdf";

      ByteArrayResource resource = new ByteArrayResource(pdfBytes);

      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
      headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
      headers.add(HttpHeaders.PRAGMA, "no-cache");
      headers.add(HttpHeaders.EXPIRES, "0");

      return ResponseEntity.ok()
          .headers(headers)
          .contentLength(pdfBytes.length)
          .contentType(MediaType.APPLICATION_PDF)
          .body(resource);

    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.internalServerError().build();
    }
  }
}
