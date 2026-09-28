package com.jemigraph.jemigraph_backend.controllers;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentInitiationResponse {
  private String transactionNumber;
  private String transactionId;
  private String message;
}
