package com.jemigraph.jemigraph_backend.DTO;

import lombok.Data;

import java.util.UUID;

@Data
public class SubscriptionRequestDTO {
  private UUID userId;
  private UUID subscriptionPlanId;
}
