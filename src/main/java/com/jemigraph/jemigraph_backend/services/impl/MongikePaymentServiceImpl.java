//package com.jemigraph.jemigraph_backend.services.impl;
//
//import com.jemigraph.jemigraph_backend.DTO.MonikerPaymentRequest;
//import com.jemigraph.jemigraph_backend.DTO.PaymentInitiationResponse;
//import com.jemigraph.jemigraph_backend.DTO.PaymentStatusResponse;
//import com.jemigraph.jemigraph_backend.Entities.Payment;
//import com.jemigraph.jemigraph_backend.Entities.SubscriptionPlan;
//import com.jemigraph.jemigraph_backend.Entities.User;
//import com.jemigraph.jemigraph_backend.enums.PaymentRepository;
//import com.jemigraph.jemigraph_backend.enums.SystemPaymentStatus;
//import com.jemigraph.jemigraph_backend.repositories.SubscriptionPlanRepository;
//import com.jemigraph.jemigraph_backend.services.PaymentSystemService;
//import com.jemigraph.jemigraph_backend.services.SubscriptionService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.Map;
//import java.util.Optional;
//import java.util.UUID;
//import java.util.concurrent.CompletableFuture;
//
//@Service("monikerPayment")
//@RequiredArgsConstructor
//public class MongikePaymentServiceImpl implements PaymentSystemService {
//    private final SubscriptionPlanRepository subscriptionPlanRepository;
//    private final SubscriptionService subscriptionService;
//    private final PaymentRepository paymentRepository;
//
//    @Value("${moniker.api.key:mk_e30f8cc15b26d1b37fd2743be7c3e49810ff8e5df7a2584d}")
//    private String apiKey;
//
//    @Override
//    public PaymentInitiationResponse initiatePaymentAsync(User user, UUID planId, String phoneNumber) {
//        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
//                .orElseThrow(() -> new RuntimeException("Subscription plan not found with ID: " + planId));
//
//        String orderId = "JEMI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
//
//
//        Payment payment = Payment.builder()
//                .orderId(orderId)
//                .userId(user.getId())
//                .planId(plan.getId())
//                .amount(plan.getPrice())
//                .phoneNumber(phoneNumber)
//                .status(SystemPaymentStatus.PENDING)
//                .build();
//
//        paymentRepository.save(payment);
//        CompletableFuture.runAsync(() -> {
//            try {
//                MonikerPaymentRequest paymentRequest = MonikerPaymentRequest.builder()
//                        .order_id(orderId)
//                        .amount(plan.getPrice().doubleValue())
//                        .buyer_phone(phoneNumber)
//                        .buyer_name(user.getEmail())
//                        .buyer_email(user.getEmail())
//                        .fee_payer("MERCHANT")
//                        .metadata(Map.of("userId", user.getId().toString(), "planId", plan.getId().toString()))
//                        .build();
//
//                HttpHeaders headers = new HttpHeaders();
//                headers.setContentType(MediaType.APPLICATION_JSON);
//                headers.set("x-api-key", apiKey);
//
//                HttpEntity<MonikerPaymentRequest> entity = new HttpEntity<>(paymentRequest, headers);
//                RestTemplate restTemplate = new RestTemplate();
//
//                String MONIKER_URL = "https://mongike.com/api/v1/payments/mobile-money/tanzania";
//                ResponseEntity<String> response = restTemplate.postForEntity(MONIKER_URL, entity, String.class);
//
//                // Update payment status based on Mongike response
//                Payment savedPayment = paymentRepository.findByOrderId(orderId)
//                        .orElseThrow(() -> new RuntimeException("Payment not found"));
//
//                if (response.getStatusCode().is2xxSuccessful()) {
//                    savedPayment.setGatewayResponse(response.getBody());
//                    // Usibadilishe status kuwa SUCCESS hapa – tungoje callback au polling
//                    // Lakini unaweza kuweka "PROCESSING" ikiwa unataka
//                    savedPayment.setStatus(SystemPaymentStatus.PENDING);
//                } else {
//                    savedPayment.setStatus(SystemPaymentStatus.FAILED);
//                    savedPayment.setGatewayResponse(response.getBody());
//                }
//                paymentRepository.save(savedPayment);
//
//            } catch (Exception e) {
//                // Log error na update payment status kuwa FAILED
//                Payment savedPayment = paymentRepository.findByOrderId(orderId)
//                        .orElseThrow(() -> new RuntimeException("Payment not found"));
//                savedPayment.setStatus(SystemPaymentStatus.FAILED);
//                savedPayment.setGatewayResponse(e.getMessage());
//                paymentRepository.save(savedPayment);
//            }
//        });
//
//        // 3. RUDISHA RESPONSE MARA MOJA (bila kusubiri Mongike)
//        return PaymentInitiationResponse.builder()
//                .orderId(orderId)
//                .status("PENDING")
//                .message("Payment request has been sent to your phone. Please confirm.")
//                .phoneNumber(phoneNumber)
//                .amount(plan.getPrice())
//                .build();
//    }
//
//
//    @Override
//    public boolean handleCallback(Map<String, Object> callbackPayload) {
//        try {
//
//            String status = (String) callbackPayload.get("status");
//            String orderId = (String) callbackPayload.get("order_id");
//            if (orderId == null) {
//                return false;
//            }
//
//
//            Optional<Payment> optionalPayment = paymentRepository.findByOrderId(orderId);
//
//            if (optionalPayment.isPresent()) {
//                Payment payment = optionalPayment.get();
//                payment.setGatewayResponse(callbackPayload.toString());
//
//
//                if ("SUCCESS".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
//                    payment.setStatus(SystemPaymentStatus.SUCCESS);
//                    paymentRepository.save(payment);
//
//
//                    @SuppressWarnings("unchecked")
//                    Map<String, Object> metadata = (Map<String, Object>) callbackPayload.get("metadata");
//
//                    if (metadata != null) {
//                        UUID userId = UUID.fromString((String) metadata.get("userId"));
//                        UUID planId = UUID.fromString((String) metadata.get("planId"));
//
//                        subscriptionService.activateSubscriptionForUser(userId, planId);
//                    }
//                    return true;
//                } else {
//
//                    payment.setStatus(SystemPaymentStatus.FAILED);
//                    paymentRepository.save(payment);
//                    return true;
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("Error handling payment callback: " + e.getMessage());
//        }
//
//        return false;
//    }
//}