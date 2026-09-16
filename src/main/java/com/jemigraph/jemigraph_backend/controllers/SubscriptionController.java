package com.jemigraph.jemigraph_backend.controllers;

import com.jemigraph.jemigraph_backend.DTO.ProcessActionDTO;
import com.jemigraph.jemigraph_backend.DTO.SubscriptionRequestDTO;
import com.jemigraph.jemigraph_backend.Entities.SubscriptionPlan;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.services.SubscriptionPlanService;
import com.jemigraph.jemigraph_backend.services.SubscriptionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subscription-plans")
@RequiredArgsConstructor
public class SubscriptionController {
  private final SubscriptionPlanService subscriptionPlanService;
  private final SubscriptionService subscriptionService;

  @GetMapping
  public ResponseEntity<List<SubscriptionPlan>> getAvailablePlans() {
    return ResponseEntity.ok(subscriptionPlanService.getAllActivePlans());
  }

  @PostMapping("/activate")
  public ResponseEntity<User> activateSubscription(
      @RequestParam UUID userId, @RequestParam UUID planTypeId) {
    User updatedUser = subscriptionService.activateSubscriptionForUser(userId, planTypeId);
    return ResponseEntity.ok(updatedUser);
  }

  @PostMapping("/request-change")
  public ResponseEntity<String> requestSubscriptionChange(
      @RequestBody SubscriptionRequestDTO request) {

    subscriptionService.sendSubscriptionApprovalRequest(
        request.getUserId(), request.getSubscriptionPlanId());

    return ResponseEntity.ok(
        "Subscription change request sent successfully. Awaiting Super Admin approval.");
  }

  // Endpoint ya pili inayofuta: Inapokea POST request kutoka frontend na ku-update database
  @PostMapping("/process-action")
  public ResponseEntity<String> processSubscriptionAction(@RequestBody ProcessActionDTO request) {
    try {
      boolean isApproved = "APPROVE".equalsIgnoreCase(request.getAction());

      // Inasimamia database update (Accept au Reject)
      subscriptionService.handleAdminAction(request.getId(), isApproved);

      return ResponseEntity.ok("Action processed successfully.");

    } catch (Exception e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }
}
