package com.jemigraph.jemigraph_backend.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jemigraph.jemigraph_backend.DTO.*;
import com.jemigraph.jemigraph_backend.Entities.Payment;
import com.jemigraph.jemigraph_backend.Entities.SubscriptionPlan;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.enums.PaymentRepository;
import com.jemigraph.jemigraph_backend.enums.SubscriptionStatus;
import com.jemigraph.jemigraph_backend.enums.SystemPaymentStatus;
import com.jemigraph.jemigraph_backend.enums.UserRole;
import com.jemigraph.jemigraph_backend.repositories.SubscriptionPlanRepository;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

  @Value("${cashpay.base-url}")
  private String baseUrl;

  @Value("${cashpay.username}")
  private String username;

  @Value("${cashpay.password}")
  private String password;

  @Override
  public PaymentInitiationResponse initiatePaymentAsync(
      User user, UUID planId, String phoneNumber) {

    try {

      if (user == null) {
        throw new RuntimeException("User is required");
      }

      LocalDateTime now = LocalDateTime.now();
      boolean hasActiveSubscription =
          user.getSubscriptionStatus() == SubscriptionStatus.ACTIVE
              && user.getSubscriptionExpiresAt() != null
              && user.getSubscriptionExpiresAt().isAfter(now);

      if (hasActiveSubscription) {
        throw new RuntimeException(
            "You already have an active subscription expiring on: "
                + user.getSubscriptionExpiresAt());
      }
      if (planId == null) {
        throw new RuntimeException("Plan ID is required");
      }
      SubscriptionPlan plan =
          subscriptionPlanRepository
              .findById(planId)
              .orElseThrow(() -> new RuntimeException("Subscription plan not found: " + planId));

      if (!plan.isActive()) {

        throw new RuntimeException("Subscription plan is inactive: " + plan.getName());
      }

      if (plan.getPrice() == null) {

        throw new RuntimeException("Subscription plan price is not configured: " + plan.getName());
      }

      BigDecimal planAmount = plan.getPrice();

      String amount = formatAmount(planAmount);

      String formattedPhone = formatPhoneNumber(phoneNumber);
      String token = getCashPayToken();
      String transactionNumber = generateTransactionNumber();

      log.info(
          "Starting CashPay payment. transactionNumber={}, user={}, plan={}, amount={}, phone={}",
          transactionNumber,
          user.getEmail(),
          plan.getName(),
          amount,
          formattedPhone);

      String referenceNumber = generatePaymentReference(token, transactionNumber);

      log.info(
          "CashPay reference generated. transactionNumber={}, referenceNumber={}",
          transactionNumber,
          referenceNumber);
      JsonNode ussdResponse = sendUssdPush(token, transactionNumber, formattedPhone, amount);

      String ussdStatus = ussdResponse.path("status").asText();

      String ussdCode = ussdResponse.path("code").asText();

      if (!"1".equals(ussdCode) || !"success".equalsIgnoreCase(ussdStatus)) {

        String description = ussdResponse.path("description").asText("CashPay USSD Push failed");

        throw new RuntimeException("CashPay USSD Push failed: " + description);
      }
      Payment payment =
          Payment.builder()
              .orderId(transactionNumber)
              .transactionNumber(transactionNumber)
              .referenceNumber(referenceNumber)
              .userId(user.getId())
              .planId(planId)
              .amount(planAmount)
              .phoneNumber(formattedPhone)
              .status(SystemPaymentStatus.PENDING)
              .gatewayResponse(ussdResponse.toString())
              .callbackAttempts(0)
              .callbackProcessed(false)
              .build();

      paymentRepository.save(payment);

      log.info(
          "CashPay payment saved as PENDING. orderId={}, transactionNumber={}, referenceNumber={}",
          transactionNumber,
          transactionNumber,
          referenceNumber);
      return PaymentInitiationResponse.builder()
          .orderId(transactionNumber)
          .status("PENDING")
          .message("USSD payment request sent successfully")
          .phoneNumber(formattedPhone)
          .amount(planAmount)
          .build();

    } catch (Exception e) {
      log.error("CashPay payment initiation failed", e);
      throw new RuntimeException("Failed to initiate CashPay payment", e);
    }
  }

  private String getCashPayToken() {

    String tokenUrl = baseUrl + "/get-token";

    HttpHeaders headers = new HttpHeaders();

    headers.setContentType(MediaType.APPLICATION_JSON);
    Map<String, Object> requestBody = new HashMap<>();

    requestBody.put("username", username);

    requestBody.put("password", password);

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

    try {

      log.debug("Requesting CashPay authentication token");

      ResponseEntity<String> response =
          restTemplate.exchange(tokenUrl, HttpMethod.POST, request, String.class);

      if (!response.getStatusCode().is2xxSuccessful()) {

        throw new RuntimeException("CashPay authentication failed: " + response.getStatusCode());
      }
      JsonNode json = objectMapper.readTree(response.getBody());
      String token = json.path("token").asText(null);

      if (token == null || token.isBlank()) {

        throw new RuntimeException("CashPay token was not returned");
      }

      log.debug("CashPay authentication successful");

      return token;

    } catch (Exception e) {

      throw new RuntimeException("Failed to authenticate with CashPay", e);
    }
  }

  // ============================================================
  // GENERATE TRANSACTION NUMBER
  // ============================================================

  private String generateTransactionNumber() {

    return "JEMI-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
  }

  private String generatePaymentReference(String token, String transactionNumber) {

    String url = baseUrl + "/generate";

    HttpHeaders headers = new HttpHeaders();

    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> requestData = new HashMap<>();

    requestData.put("transactionNumber", transactionNumber);

    Map<String, Object> body = new HashMap<>();

    body.put("request", requestData);

    Map<String, Object> header = new HashMap<>();

    header.put("token", token);

    Map<String, Object> payload = new HashMap<>();

    payload.put("header", header);

    payload.put("body", body);

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

    try {

      ResponseEntity<String> response =
          restTemplate.exchange(url, HttpMethod.POST, request, String.class);

      if (!response.getStatusCode().is2xxSuccessful()) {

        throw new RuntimeException(
            "CashPay reference generation failed: " + response.getStatusCode());
      }

      JsonNode json = objectMapper.readTree(response.getBody());

      String code = json.path("code").asText();

      if (!"1".equals(code)) {

        String message = json.path("message").asText("Failed to generate payment reference");

        throw new RuntimeException("CashPay generate failed: " + message);
      }

      String referenceNumber = json.path("referenceNumber").asText(null);

      if (referenceNumber == null || referenceNumber.isBlank()) {

        throw new RuntimeException("CashPay did not return reference number");
      }

      return referenceNumber;

    } catch (RuntimeException e) {

      throw e;

    } catch (Exception e) {

      throw new RuntimeException("Failed to parse CashPay generate response", e);
    }
  }

  private JsonNode sendUssdPush(
      String token, String transactionNumber, String phoneNumber, String amount) {

    String url = baseUrl + "/ussdpush";

    HttpHeaders headers = new HttpHeaders();

    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, Object> requestData = new HashMap<>();

    requestData.put("transactionNumber", transactionNumber);

    requestData.put("phone", phoneNumber);

    requestData.put("amount", amount);

    requestData.put("type", "ussdpush");

    Map<String, Object> body = new HashMap<>();

    body.put("request", requestData);

    Map<String, Object> header = new HashMap<>();
    header.put("token", token);

    // --------------------------------------------------------
    // COMPLETE PAYLOAD
    // --------------------------------------------------------

    Map<String, Object> payload = new HashMap<>();

    payload.put("header", header);

    payload.put("body", body);

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

    log.info(
        "Sending CashPay USSD push. transactionNumber={}, phone={}, amount={}",
        transactionNumber,
        phoneNumber,
        amount);

    try {

      ResponseEntity<String> response =
          restTemplate.exchange(url, HttpMethod.POST, request, String.class);

      if (!response.getStatusCode().is2xxSuccessful()) {

        throw new RuntimeException("CashPay USSD Push failed: " + response.getStatusCode());
      }

      JsonNode json = objectMapper.readTree(response.getBody());

      log.info("CashPay USSD response: {}", json);

      return json;

    } catch (Exception e) {

      throw new RuntimeException("Failed to send CashPay USSD Push", e);
    }
  }

  private String formatAmount(BigDecimal price) {

    if (price == null) {

      throw new IllegalArgumentException("Payment amount cannot be null");
    }

    if (price.compareTo(BigDecimal.ZERO) <= 0) {

      throw new IllegalArgumentException("Payment amount must be greater than zero");
    }

    return price.stripTrailingZeros().toBigInteger().toString();
  }

  private String formatPhoneNumber(String phoneNumber) {

    if (phoneNumber == null || phoneNumber.isBlank()) {

      throw new IllegalArgumentException("Phone number is required");
    }

    String phone = phoneNumber.trim().replaceAll("\\s+", "").replace("-", "");

    if (phone.startsWith("+255")) {

      phone = phone.substring(1);
    } else if (phone.startsWith("0")) {

      phone = "255" + phone.substring(1);
    } else if (phone.length() == 9 && phone.startsWith("7")) {

      phone = "255" + phone;
    }

    if (!phone.matches("255\\d{9}")) {

      throw new IllegalArgumentException(
          "Invalid Tanzanian phone number. " + "Expected 255XXXXXXXXX");
    }

    return phone;
  }

  @Override
  public boolean handleCallback(PaymentCallbackDto callbackPayload) {
    try {
      if (callbackPayload == null) {
        log.warn("Received empty CashPay callback");
        return false;
      }
      String transactionNumber = callbackPayload.getTransactionNumber();
      String paymentStatus = callbackPayload.getStatus();
      Object referenceNumberObj = callbackPayload.getReferenceNumber();
      Object receiptNumberObj = callbackPayload.getReceiptNumber();
      Object providerObj = callbackPayload.getProvider();
      Object amountObj = callbackPayload.getAmount();
      Object phoneObj = callbackPayload.getPhone();
      if (transactionNumber == null
          || transactionNumber.trim().isEmpty()
          || paymentStatus == null
          || paymentStatus.trim().isEmpty()) {
        log.warn("Invalid CashPay callback: missing transactionNumber or status");
        return false;
      }

      String transaction = transactionNumber;

      log.info(
          "CashPay callback received. transactionNumber={}, status={}, referenceNumber={}, receiptNumber={}, provider={}, amount={}, phone={}",
          transaction,
          paymentStatus,
          referenceNumberObj,
          receiptNumberObj,
          providerObj,
          amountObj,
          phoneObj);

      Payment payment = paymentRepository.findByTransactionNumber(transaction).orElse(null);

      if (payment == null) {
        log.warn("Payment not found for CashPay transactionNumber={}", transaction);
        return false;
      }

      if (payment.isCallbackProcessed()) {
        log.info("CashPay callback already processed. transactionNumber={}", transaction);
        return true;
      }

      if (referenceNumberObj != null && payment.getReferenceNumber() != null) {
        String callbackReference = referenceNumberObj.toString();
        if (!payment.getReferenceNumber().equals(callbackReference)) {
          log.warn(
              "CashPay reference mismatch. transactionNumber={}, expected={}, received={}",
              transaction,
              payment.getReferenceNumber(),
              callbackReference);
          return false;
        }
      }

      if (amountObj != null && payment.getAmount() != null) {
        try {
          BigDecimal callbackAmount = new BigDecimal(amountObj.toString());
          if (payment.getAmount().compareTo(callbackAmount) != 0) {
            log.warn(
                "CashPay amount mismatch. transactionNumber={}, expected={}, received={}",
                transaction,
                payment.getAmount(),
                callbackAmount);
            return false;
          }
        } catch (NumberFormatException e) {
          log.warn("Invalid amount in CashPay callback: {}", amountObj);
          return false;
        }
      }

      payment.setCallbackResponse(callbackPayload.toString());
      payment.setCallbackReceivedAt(LocalDateTime.now());

      int attempts = payment.getCallbackAttempts() == null ? 0 : payment.getCallbackAttempts();
      payment.setCallbackAttempts(attempts + 1);

      if (referenceNumberObj != null) {
        payment.setReferenceNumber(referenceNumberObj.toString());
      }

      if (receiptNumberObj != null) {
        payment.setReceiptNumber(receiptNumberObj.toString());
        payment.setProviderTransactionId(receiptNumberObj.toString());
      }

      if (providerObj != null) {
        payment.setProvider(providerObj.toString());
      }

      User user =
          userRepository
              .findById(payment.getUserId())
              .orElseThrow(
                  () -> new RuntimeException("User not found for payment: " + payment.getId()));

      if ("SUCCESS".equalsIgnoreCase(paymentStatus)) {
        payment.setStatus(SystemPaymentStatus.SUCCESS);
        payment.setCallbackProcessed(true);
        paymentRepository.save(payment);

        log.info(
            "CashPay payment marked SUCCESS. transactionNumber={}, paymentId={}",
            transaction,
            payment.getId());

        subscriptionService.activateSubscription(payment.getUserId(), payment.getPlanId());

        log.info(
            "Subscription activated. userId={}, planId={}",
            payment.getUserId(),
            payment.getPlanId());

        try {
          sendPaymentWebSocketNotification(
              payment, "SUCCESS", "Payment successful. Your subscription is now active.");
        } catch (Exception e) {
          log.error(
              "Failed to send payment SUCCESS WebSocket notification. orderId={}",
              payment.getOrderId(),
              e);
        }

        return true;
      }

      if ("FAILED".equalsIgnoreCase(paymentStatus)) {
        payment.setStatus(SystemPaymentStatus.FAILED);
        payment.setCallbackProcessed(true);
        paymentRepository.save(payment);

        log.warn(
            "CashPay payment marked FAILED. transactionNumber={}, paymentId={}",
            transaction,
            payment.getId());

        try {
          sendPaymentWebSocketNotification(
              payment, "FAILED", "Your payment could not be completed. Please try again.");
        } catch (Exception e) {
          log.error(
              "Failed to send payment FAILED WebSocket notification. orderId={}",
              payment.getOrderId(),
              e);
        }

        return true;
      }

      log.warn(
          "Unknown CashPay callback status. transactionNumber={}, status={}",
          transaction,
          paymentStatus);
      return false;

    } catch (Exception e) {
      log.error("Failed to process CashPay callback", e);
      return false;
    }
  }

  @Override
  public SubscriptionPaymentResponseDTO getPaymentStatusResponse(String orderId) {

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

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime paymentDate = payment.getCreatedAt() != null ? payment.getCreatedAt() : now;

    String startDate = "";
    String endDate = "";
    String planName = "N/A";
    String planDescription = "";
    int durationInDays = 0;

    if (plan != null) {
      planName = plan.getName() != null ? plan.getName().toString() : "N/A";
      planDescription = plan.getDescription() != null ? plan.getDescription() : "";
      durationInDays = plan.getDurationInDays() != null ? plan.getDurationInDays() : 0;

      startDate = paymentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
      endDate =
          paymentDate.plusDays(durationInDays).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    boolean isActive =
        user.getSubscriptionStatus() == SubscriptionStatus.ACTIVE
            && user.getSubscriptionExpiresAt() != null
            && user.getSubscriptionExpiresAt().isAfter(now);

    return SubscriptionPaymentResponseDTO.builder()
        .orderId(payment.getOrderId())
        .status(payment.getStatus() != null ? payment.getStatus().name() : null)
        .amount(payment.getAmount())
        .planId(payment.getPlanId() != null ? payment.getPlanId().toString() : null)
        .transactionNumber(payment.getTransactionNumber())
        .referenceNumber(payment.getReferenceNumber())
        .receiptNumber(payment.getReceiptNumber())
        .phoneNumber(payment.getPhoneNumber())
        .planName(planName)
        .planDescription(planDescription)
        .durationInDays(durationInDays)
        .startDate(startDate)
        .endDate(endDate)
        .subscriptionActive(isActive)
        .subscriptionStatus(isActive ? "ACTIVE" : (plan != null ? "INACTIVE" : "N/A"))
        .build();
  }

  @Override
  public List<SubscriberResponseDto> getAllSubscribers() {
    List<User> photographers =
        userRepository.findAll().stream()
            .filter(user -> user.getRole() == UserRole.PHOTOGRAPHER)
            .toList();
    List<SubscriberResponseDto> subscribersList = new ArrayList<>();
    for (User user : photographers) {
      if (user.getSubscriptionStatus() != null) {
        SubscriptionPlan activePlan = null;
        Payment latestPayment =
            paymentRepository
                .findFirstByUserIdAndStatusOrderByCreatedAtDesc(
                    user.getId(), SystemPaymentStatus.SUCCESS)
                .orElse(null);
        if (latestPayment != null && latestPayment.getPlanId() != null) {
          activePlan = subscriptionPlanRepository.findById(latestPayment.getPlanId()).orElse(null);
        }
        SubscriberResponseDto dto =
            SubscriberResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .subscriptionStatus(user.getSubscriptionStatus().name())
                .expiresAt(user.getSubscriptionExpiresAt())
                .planName(activePlan != null ? activePlan.getName().toString() : "N/A")
                .planAmount(activePlan != null ? activePlan.getPrice() : null)
                .durationInDays(activePlan != null ? activePlan.getDurationInDays() : 0)
                .build();

        subscribersList.add(dto);
      }
    }
    return subscribersList;
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

  private void sendPaymentWebSocketNotification(Payment payment, String status, String message) {
    if (payment == null) {

      throw new IllegalArgumentException("Payment cannot be null");
    }

    if (payment.getOrderId() == null || payment.getOrderId().isBlank()) {

      throw new IllegalArgumentException("Payment orderId cannot be null");
    }

    PaymentStatusResponse response =
        PaymentStatusResponse.builder()
            .orderId(payment.getOrderId())
            .status(status)
            .message(message)
            .amount(payment.getAmount())
            .receiptNumber(payment.getReceiptNumber())
            .build();
    String destination = "/topic/payment/" + payment.getOrderId();
    messagingTemplate.convertAndSend(destination, response);

    log.info(
        "Payment WebSocket notification sent. " + "destination={}, orderId={}, status={}",
        destination,
        payment.getOrderId(),
        status);
  }
}
