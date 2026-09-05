package com.jemigraph.jemigraph_backend.controllers;

import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.enums.AccessStatus;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionsController {

    private final UserRepository userRepository;


    @GetMapping("/my-status")
    public ResponseEntity<?> getMySubscriptionStatus(Principal authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );


        AccessStatus accessStatus = user.getAccessStatus();


        Map<String, Object> response = new HashMap<>();

        response.put("subscriptionStatus", user.getSubscriptionStatus());
        response.put("accessStatus", accessStatus);
        response.put("trialEndsAt", user.getTrialEndsAt());
        response.put("subscriptionExpiresAt", user.getSubscriptionExpiresAt());


        return ResponseEntity.ok(response);
    }

}