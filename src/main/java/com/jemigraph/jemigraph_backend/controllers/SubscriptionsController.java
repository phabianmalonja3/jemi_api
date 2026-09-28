package com.jemigraph.jemigraph_backend.controllers;

import com.jemigraph.jemigraph_backend.DTO.MnoCheckoutResponse;
import com.jemigraph.jemigraph_backend.DTO.PaymentInitRequestDTO;
import com.jemigraph.jemigraph_backend.Entities.Subscription;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.services.AzamPesaService;
import com.jemigraph.jemigraph_backend.services.PaymentSystemService;
import com.jemigraph.jemigraph_backend.services.SubscriptionService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionsController {

  private final UserRepository userRepository;
  private final SubscriptionService subscriptionService;
  private final PaymentSystemService paymentService;
  private final AzamPesaService azamPesaService;

  @PreAuthorize("hasRole('PHOTOGRAPHER')")
  @GetMapping("/me")
  public ResponseEntity<?> getMySubscription(Principal authentication) {

    User user =
        userRepository
            .findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

    Subscription subscription = subscriptionService.getByUserId(user.getId());

    Map<String, Object> response = new HashMap<>();
    if (subscription == null) {
      response.put("id", null);
      response.put("status", "NONE");
      response.put("package", null);
      response.put("price", BigDecimal.ZERO);
      response.put("startedAt", null);
      response.put("expiresAt", null);

      return ResponseEntity.ok(response);
    }

    // User ana subscription
    response.put("id", subscription.getId());
    response.put("status", subscription.getStatus());

    if (subscription.getSubscriptionPackage() != null) {
      response.put("package", subscription.getSubscriptionPackage().getName());

      response.put("price", subscription.getSubscriptionPackage().getPrice());
    } else {
      response.put("package", null);
      response.put("price", BigDecimal.ZERO);
    }

    response.put("startedAt", subscription.getStartedAt());
    response.put("expiresAt", subscription.getExpiresAt());

    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasRole('PHOTOGRAPHER')")
  @PostMapping("/pay")
  public ResponseEntity<MnoCheckoutResponse> initiatePayment(
          Principal principal, @Valid @RequestBody PaymentInitRequestDTO paymentInitRequestDTO) {

    User currentUser =
            userRepository
                    .findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

    MnoCheckoutResponse response =
            azamPesaService.mnoCheckout(
                    currentUser,
                    paymentInitRequestDTO.getPlanId(),
                    paymentInitRequestDTO.getPhoneNumber(),
                    paymentInitRequestDTO.getProvider());
    return ResponseEntity.ok(response);
  }

}
