package com.jemigraph.jemigraph_backend.listeners;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jemigraph.jemigraph_backend.Entities.Payment;
import com.jemigraph.jemigraph_backend.Entities.SubscriptionPlan;
import com.jemigraph.jemigraph_backend.enums.PaymentRepository;
import com.jemigraph.jemigraph_backend.enums.SystemPaymentStatus;
import com.jemigraph.jemigraph_backend.events.PaymentInitiationEvent;
import com.jemigraph.jemigraph_backend.repositories.SubscriptionPlanRepository;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.services.SubscriptionService;
import com.jemigraph.jemigraph_backend.services.impl.CashPayServiceImpl;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.http.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

	private final CashPayServiceImpl cashPayService;
	private final SubscriptionPlanRepository subscriptionPlanRepository;
	private final PaymentRepository paymentRepository;
	// Rekebisha hapa iwe Repository ya JPA ya Payment
	private final UserRepository userRepository;
	private final SubscriptionService subscriptionService;
	private final SimpMessagingTemplate messagingTemplate;
	private final ApplicationEventPublisher eventPublisher;
	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;

	@Value("${cashpay.base-url}")
	private String baseUrl;

	@Value("${cashpay.username}")
	private String username;

	@Value("${cashpay.password}")
	private String password;

	@Async
	@EventListener
	public void handlePaymentInitiation(PaymentInitiationEvent event) {
		try {
			log.info(
					"Processing background USSD push for user={}, planId={}, phone={}",
					event.user().getEmail(),
					event.planId(),
					event.phoneNumber());

			SubscriptionPlan plan =
					subscriptionPlanRepository
							.findById(event.planId())
							.orElseThrow(() -> new RuntimeException("Subscription plan not found: " + event.planId()));

			if (!plan.isActive()) {
				throw new RuntimeException("Subscription plan is inactive: " + plan.getName());
			}

			if (plan.getPrice() == null) {
				throw new RuntimeException("Subscription plan price is not configured: " + plan.getName());
			}

			BigDecimal planAmount = plan.getPrice();
			String amount = formatAmount(planAmount);
			String formattedPhone = formatPhoneNumber(event.phoneNumber());
			String token = getCashPayToken();
			String transactionNumber = generateTransactionNumber();

			log.info(
					"Starting background CashPay payment. transactionNumber={}, user={}, plan={}, amount={}, phone={}",
					transactionNumber,
					event.user().getEmail(),
					plan.getName(),
					amount,
					formattedPhone);

			String referenceNumber = generatePaymentReference(token, transactionNumber);

			log.info(
					"CashPay reference generated in background. transactionNumber={}, referenceNumber={}",
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
							.userId(event.user().getId())
							.planId(event.planId())
							.amount(planAmount)
							.phoneNumber(formattedPhone)
							.status(SystemPaymentStatus.PENDING)
							.gatewayResponse(ussdResponse.toString())
							.callbackAttempts(0)
							.callbackProcessed(false)
							.build();

			paymentRepository.save(payment);

			log.info(
					"CashPay payment successfully processed and saved as PENDING in background. orderId={}, transactionNumber={}, referenceNumber={}",
					transactionNumber,
					transactionNumber,
					referenceNumber);

		} catch (Exception e) {
			log.error("Failed to process background payment initiation", e);
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
				throw new RuntimeException("CashPay reference generation failed: " + response.getStatusCode());
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
					"Invalid Tanzanian phone number. Expected 255XXXXXXXXX");
		}
		return phone;
	}
}