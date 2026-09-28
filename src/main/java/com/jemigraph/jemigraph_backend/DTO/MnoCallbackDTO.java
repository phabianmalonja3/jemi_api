package com.jemigraph.jemigraph_backend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MnoCallbackDTO {

  private String message;
  private String user;
  private String password;
  private String clientId;

  @JsonProperty("transactionstatus")
  private String transactionStatus;

  private String operator;
  private String reference;

  @JsonProperty("externalreference")
  private String externalReference;

  @JsonProperty("utilityref")
  private String utilityRef;

  private String amount;
  private String transid;
  private String msisdn;

  @JsonProperty("mnoreference")
  private String mnoReference;

  @JsonProperty("submerchantAcc")
  private String subMerchantAcc;

  @JsonProperty("additionalProperties")
  private Map<String, Object> additionalProperties;

  private String signature;
}
