package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.DTO.MnoCallbackDTO;
import com.jemigraph.jemigraph_backend.DTO.MnoCheckoutResponse;
import com.jemigraph.jemigraph_backend.Entities.Payment;
import com.jemigraph.jemigraph_backend.Entities.Subscription;
import com.jemigraph.jemigraph_backend.Entities.SubscriptionPlan;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.configs.AzamPesaConfig;
import com.jemigraph.jemigraph_backend.enums.PaymentRepository;
import com.jemigraph.jemigraph_backend.enums.SubscriptionStatus;
import com.jemigraph.jemigraph_backend.enums.SystemPaymentStatus;
import com.jemigraph.jemigraph_backend.exceptions.AzamPayAuthenticationException;
import com.jemigraph.jemigraph_backend.repositories.SubscriptionPlanRepository;
import com.jemigraph.jemigraph_backend.repositories.SubscriptionRepository;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.schedulers.AzamTokenManager;
import com.jemigraph.jemigraph_backend.services.AzamPesaService;
import com.jemigraph.jemigraph_backend.utils.CallbackSignatureVerifier;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AzamPesaServiceImpl implements AzamPesaService {
  private final AzamPesaConfig azamPesaConfig;
  private final RestTemplate restTemplate;
  private final SubscriptionPlanRepository subscriptionPlanRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final PaymentRepository paymentRepository;
  private final AzamTokenManager azamTokenManager;
  private final CallbackSignatureVerifier callbackSignatureVerifier;
  private final UserRepository userRepository;

  @Override
  public MnoCheckoutResponse mnoCheckout(
      User user, UUID planId, String phoneNumber, String provider) {
    if (user == null) {
      throw new RuntimeException("User is required");
    }

    if (planId == null) {
      throw new RuntimeException("Plan ID is required");
    }

    LocalDateTime now = LocalDateTime.now();

    Subscription subscription = subscriptionRepository.findByUserId(user.getId()).orElse(null);

    if (subscription != null
        && subscription.getStatus() == SubscriptionStatus.ACTIVE
        && subscription.getExpiresAt() != null
        && subscription.getExpiresAt().isAfter(now)) {

      throw new RuntimeException(
          "You already have an active subscription expiring on: " + subscription.getExpiresAt());
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
    String transactionNumber = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    String externalId = "EXT-" + UUID.randomUUID();
    String token = azamTokenManager.getActiveToken();
    if (token == null || token.isEmpty()) {
      throw new AzamPayAuthenticationException(
          "The system currently lacks a valid AzamPay token. Please try again later.");
    }
    String url = azamPesaConfig.getBaseUrl() + "/azampay/mno/checkout";
    Map<String, Object> fullPayload = new HashMap<>();
    Map<String, Object> additionalProperties = new HashMap<>();
    fullPayload.put("accountNumber", phoneNumber);
    fullPayload.put("amount", planAmount.doubleValue());
    fullPayload.put("provider", provider);
    fullPayload.put("currency", "TZS");
    fullPayload.put("externalId", externalId);
    additionalProperties.put("userId", user.getId().toString());
    additionalProperties.put("planId", planId.toString());
    fullPayload.put("additionalProperties", additionalProperties);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(token);
    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(fullPayload, headers);

    try {

      ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

      if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
        throw new RuntimeException("Imeshindikana kupata jibu sahihi kutoka AzamPay");
      }

      Map azamResponse = response.getBody();

      String msg = "";

      Boolean success = (Boolean) azamResponse.get("success");
      if (success == null || !success) {
        msg = (String) azamResponse.getOrDefault("message", "AzamPay checkout imeshindwa");
        throw new RuntimeException("AzamPay Error: " + msg);
      }

      String azamTransactionId = (String) azamResponse.get("transactionId");
      Payment payment =
          Payment.builder()
              .orderId(transactionNumber)
              .transactionNumber(transactionNumber)
              .referenceNumber(azamTransactionId)
              .userId(user.getId())
              .planId(planId)
              .amount(planAmount)
              .transactionId(azamTransactionId)
              .phoneNumber(phoneNumber)
              .status(SystemPaymentStatus.PENDING)
              .gatewayResponse(azamResponse.toString())
              .callbackAttempts(0)
              .callbackProcessed(false)
              .build();

      paymentRepository.save(payment);
      log.info(
          "Ombi la malipo limetumwa na kuhifadhiwa kwa mafanikio. TransactionId ya AzamPay: {}",
          azamTransactionId);

      return MnoCheckoutResponse.builder()
          .message(msg)
          .status(true)
          .transactionId(azamTransactionId)
          .transactionNumber(transactionNumber)
          .build();
    } catch (Exception e) {
      log.error("Hitilafu imetokea wakati wa kufanya AzamPay checkout: {}", e.getMessage());
      throw new RuntimeException("Failed to initiate AzamPay payment: " + e.getMessage(), e);
    }
  }

  @Override
  public void processCallback(MnoCallbackDTO callbackPayload) {

    String utilityRef = callbackPayload.getUtilityRef();
    String externalReference = callbackPayload.getExternalReference();
    String transactionStatus = callbackPayload.getTransactionStatus();
    String operator = callbackPayload.getOperator();
    String signature = callbackPayload.getSignature();
    String transId = callbackPayload.getTransid();
    String mnoReference = callbackPayload.getMnoReference();

    if (transId == null || transId.isEmpty()) {
      throw new RuntimeException("transid is missing in callback");
    }

    String publicKeyPem = azamTokenManager.getActivePublicKey();
    if (publicKeyPem == null || publicKeyPem.isEmpty()) {
      log.error("Public Key haipatikani kwenye RAM wakati wa kuhakiki callback.");
      throw new RuntimeException("Public Key is missing");
    }

    boolean isSignatureValid =
        callbackSignatureVerifier.verify(
            utilityRef, externalReference, transactionStatus, operator, signature, publicKeyPem);

    if (!isSignatureValid) {
      throw new RuntimeException("Unauthorized: Invalid digital signature");
    }

    Payment payment =
        paymentRepository
            .findByTransactionId(transId)
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Malipo hayaonekani kwenye mfumo kwa transid: " + transId));

    if (payment.getStatus() == SystemPaymentStatus.SUCCESS) {
      log.info("Muamala huu ulikwishawahi kushughuliciwa tayari kuwa SUCCESS: {}", transId);
      return;
    }

    boolean isSuccessful = "success".equalsIgnoreCase(transactionStatus);
    payment.setAzamReference(callbackPayload.getReference());
    payment.setExternalReference(callbackPayload.getExternalReference());
    payment.setUtilityRef(callbackPayload.getUtilityRef());
    payment.setMsisdn(callbackPayload.getMsisdn());
    payment.setCallbackMessage(callbackPayload.getMessage());
    payment.setProviderTransactionId(mnoReference);
    payment.setCallbackResponse(callbackPayload.toString());
    payment.setCallbackReceivedAt(LocalDateTime.now());
    payment.setCallbackAttempts(payment.getCallbackAttempts() + 1);

    if (isSuccessful) {
      payment.setStatus(SystemPaymentStatus.SUCCESS);
      payment.setReferenceNumber(callbackPayload.getReference());
      payment.setCallbackProcessed(true);
      paymentRepository.save(payment);

      User user =
          userRepository
              .findById(payment.getUserId())
              .orElseThrow(() -> new RuntimeException("Mtumiaji haonekani"));

      SubscriptionPlan plan =
          subscriptionPlanRepository
              .findById(payment.getPlanId())
              .orElseThrow(() -> new RuntimeException("Plan haionekani"));

      LocalDateTime now = LocalDateTime.now();
      LocalDateTime expiresAt =
          now.plusDays(plan.getDurationInDays() > 0 ? plan.getDurationInDays() : 30);

      Subscription subscription =
          subscriptionRepository.findByUserId(user.getId()).orElse(new Subscription());
      subscription.setUser(user);
      subscription.setStatus(SubscriptionStatus.ACTIVE);
      subscription.setStartedAt(now);
      subscription.setExpiresAt(expiresAt);

      subscriptionRepository.save(subscription);
      log.info("Subscription imewashwa kwa mafanikio kwa mtumiaji: {}", user.getId());

    } else {
      payment.setStatus(SystemPaymentStatus.FAILED);
      payment.setCallbackProcessed(true);
      paymentRepository.save(payment);

      log.warn("Malipo yamefeli kutoka AzamPay. Hali iliyokuja: {}", transactionStatus);
    }
  }

  @Override
  public List<Map<String, Object>> getPaymentPartners() {
    String url = azamPesaConfig.getBaseUrl() + "/api/v1/Partner/GetPaymentPartners";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(azamTokenManager.getActiveToken());
    headers.set("User-Agent", "Jemigraph-Backend-Service");
    headers.set("Accept", "application/json");

    HttpEntity<Void> entity = new HttpEntity<>(headers);

    try {

      ResponseEntity<List> response =
          restTemplate.exchange(url, HttpMethod.GET, entity, List.class);

      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        return (List<Map<String, Object>>) response.getBody();
      }
    } catch (Exception e) {
      log.error("Hitilafu imetokea wakati wa kuchota Payment Partners: {}", e.getMessage());
      throw new RuntimeException("Imeshindwa kupata watoa huduma za malipo: " + e.getMessage());
    }

    return Collections.emptyList();
  }
}
