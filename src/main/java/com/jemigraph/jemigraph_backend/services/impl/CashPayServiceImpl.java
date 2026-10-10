package com.jemigraph.jemigraph_backend.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jemigraph.jemigraph_backend.DTO.*;
import com.jemigraph.jemigraph_backend.Entities.Payment;
import com.jemigraph.jemigraph_backend.Entities.PaymentRepository;
import com.jemigraph.jemigraph_backend.Entities.Subscription;
import com.jemigraph.jemigraph_backend.Entities.SubscriptionPlan;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.enums.SubscriptionStatus;
import com.jemigraph.jemigraph_backend.repositories.SubscriptionPlanRepository;
import com.jemigraph.jemigraph_backend.repositories.SubscriptionRepository;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.services.EmailService;
import com.jemigraph.jemigraph_backend.services.PaymentSystemService;
import com.jemigraph.jemigraph_backend.services.SubscriptionService;
import com.jemigraph.jemigraph_backend.utils.BulkReceiptGenerator;
import com.jemigraph.jemigraph_backend.utils.ReceiptGenerator;
import io.jsonwebtoken.io.IOException;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class CashPayServiceImpl implements PaymentSystemService {

  private static final Logger log = LoggerFactory.getLogger(CashPayServiceImpl.class);

  private final RestTemplate restTemplate;

  private final ObjectMapper objectMapper;

  private final SubscriptionPlanRepository subscriptionPlanRepository;

  private final PaymentRepository paymentRepository;

  private final UserRepository userRepository;

  private final SubscriptionService subscriptionService;

  private final SimpMessagingTemplate messagingTemplate;
  private final EmailService emailService;
  private final SubscriptionRepository subscriptionRepository;

  @Value("${cashpay.base-url}")
  private String baseUrl;

  @Value("${cashpay.username}")
  private String username;

  @Value("${cashpay.password}")
  private String password;



  @Override
  public SubscriptionPaymentResponseDTO getPaymentStatusResponse(String transactionId) {
    log.info("🔍 Fetching payment status for orderId={}", transactionId);

    Payment payment =
        paymentRepository
            .findByTransactionId(transactionId)
            .orElseThrow(() -> new RuntimeException("Payment not found for orderId: " + transactionId));

    log.info("✅ Payment found: id={}, status={}", payment.getId(), payment.getStatus());

    User user =
        userRepository
            .findById(payment.getUserId())
            .orElseThrow(
                () -> new RuntimeException("User not found for payment: " + payment.getId()));

    SubscriptionPlan plan =
        subscriptionPlanRepository
            .findById(payment.getPlanId())
            .orElseThrow(
                () -> new RuntimeException("Plan not found for payment: " + payment.getId()));

    // 4. Pata subscription (kama ipo)
    Subscription subscription =
        subscriptionRepository.findByUserId(payment.getUserId()).orElse(null);

    SubscriptionPaymentResponseDTO response = new SubscriptionPaymentResponseDTO();
    response.setStatus(payment.getStatus().name()); // SUCCESS / PENDING / FAILED
    response.setOrderId(payment.getTransactionNumber());
    response.setAmount(BigDecimal.valueOf(payment.getAmount().doubleValue()));
    response.setTransactionNumber(payment.getTransactionNumber());
    response.setReferenceNumber(
        payment.getReferenceNumber() != null ? payment.getReferenceNumber() : "N/A");
    response.setPhoneNumber(payment.getPhoneNumber() != null ? payment.getPhoneNumber() : "N/A");
    response.setPlanId(plan.getId().toString());
    response.setPlanName(String.valueOf(plan.getName()));
    response.setPlanDescription(plan.getDescription() != null ? plan.getDescription() : "");
    response.setDurationInDays(plan.getDurationInDays() != null ? plan.getDurationInDays() : 30);
    if (subscription != null) {
      response.setStartDate(
          subscription.getStartedAt() != null ? subscription.getStartedAt().toString() : "");
      response.setEndDate(
          subscription.getExpiresAt() != null ? subscription.getExpiresAt().toString() : "");
      response.setSubscriptionActive(subscription.getStatus() == SubscriptionStatus.ACTIVE);
      response.setSubscriptionStatus(subscription.getStatus().name());
    } else {
      response.setStartDate("");
      response.setEndDate("");
      response.setSubscriptionActive(false);
      response.setSubscriptionStatus("INACTIVE");
    }

    log.info(
        "✅ Response: status={}, planName={}, subscriptionActive={}",
        response.getStatus(),
        response.getPlanName(),
        response.isSubscriptionActive());

    return response;
  }

  @Override
  public List<SubscriberResponseDto> getAllSubscribers() {

    return subscriptionRepository.findAll().stream()
        .map(
            subscription -> {
              User user = subscription.getUser();

              SubscriptionPlan plan = subscription.getSubscriptionPackage();

              return SubscriberResponseDto.builder()
                  .userId(user.getId())
                  .email(user.getEmail())
                  .subscriptionStatus(
                      subscription.getStatus() != null ? subscription.getStatus().name() : null)
                  .expiresAt(subscription.getExpiresAt())
                  .planName(plan != null && plan.getName() != null ? plan.getName().name() : null)
                  .planAmount(plan != null ? plan.getPrice() : null)
                  .durationInDays(plan != null ? plan.getDurationInDays() : null)
                  .build();
            })
        .toList();
  }

  @Override
  public byte[] generateReceiptPdf(String orderId) throws Exception {
    ZoneId tz = ZoneId.of("Africa/Dar_es_Salaam");
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    Payment payment =
        paymentRepository
            .findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Payment not found with orderId: " + orderId));
    User user =
        userRepository
            .findById(payment.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));
    SubscriptionPlan plan = null;
    if (payment.getPlanId() != null) {
      plan = subscriptionPlanRepository.findById(payment.getPlanId()).orElse(null);
    }
    String planName = plan != null ? plan.getName().toString() : "N/A";
    int durationInDays = plan != null ? plan.getDurationInDays() : 30;
    LocalDateTime paymentDate =
        payment.getCreatedAt() != null ? payment.getCreatedAt() : LocalDateTime.now(tz);
    String startDate = paymentDate.format(dateFormatter);
    LocalDateTime expiryDate = paymentDate.plusDays(durationInDays);
    String endDate = expiryDate.format(dateFormatter);
    String fileName = "receipt_" + orderId + ".pdf";
    String filePath = System.getProperty("java.io.tmpdir") + "/" + fileName;
    String logoPath = "";
    try {
      logoPath = new ClassPathResource("static/logo.png").getFile().getAbsolutePath();
    } catch (Exception e) {
      logoPath = null;
    }

    ReceiptGenerator.generateReceipt(
        filePath,
        payment.getTransactionNumber() != null
            ? payment.getTransactionNumber()
            : payment.getOrderId(),
        String.valueOf(payment.getAmount()),
        payment.getPhoneNumber() != null ? payment.getPhoneNumber() : "N/A",
        payment.getReceiptNumber() != null ? payment.getReceiptNumber() : "N/A",
        logoPath,
        planName,
        startDate,
        endDate,
        durationInDays);
    File file = new File(filePath);
    Path path = file.toPath();
    byte[] pdfBytes = Files.readAllBytes(path);
    file.delete();
    return pdfBytes;
  }

  @Override
  public List<Payment> getPaymentsByUserAndDateRange(
      UUID userId, LocalDateTime startDate, LocalDateTime endDate) {

    if (startDate != null && endDate != null) {
      return paymentRepository.findByUserIdAndCreatedAtBetween(userId, startDate, endDate);
    } else if (startDate != null) {
      return paymentRepository.findByUserIdAndCreatedAtAfter(userId, startDate);
    } else if (endDate != null) {
      return paymentRepository.findByUserIdAndCreatedAtBefore(userId, endDate);
    } else {
      return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
  }

  @Override
  public byte[] generateBulkReceiptPdf(List<Payment> payments, User user) {
    String logoPath = null;
    try {
      logoPath = new ClassPathResource("static/logo.png").getFile().getAbsolutePath();
    } catch (Exception e) {
      logoPath = null;
    }

    BigDecimal totalAmount =
        payments.stream()
            .map(Payment::getAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    try {
      return BulkReceiptGenerator.generateBulkReceiptBytes(
          payments, user.getName(), user.getEmail(), totalAmount, logoPath);
    } catch (IOException e) {
      throw new RuntimeException("Failed to generate bulk receipt PDF", e);
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Payment getPaymentByIdAndUser(UUID id) {
    return paymentRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
  }

  @Override
  public PaymentStatusResponse getPaymentStatus(String orderId) {
    Payment payment =
        paymentRepository
            .findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
    return PaymentStatusResponse.builder()
        .orderId(payment.getOrderId())
        .status(payment.getStatus().name())
        .amount(payment.getAmount())
        .build();
  }



}
