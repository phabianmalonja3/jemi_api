package com.jemigraph.jemigraph_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MnoCheckoutResponse {
  private String transactionNumber;
  private String transactionId;
  private String message;
  private Boolean status;
}
