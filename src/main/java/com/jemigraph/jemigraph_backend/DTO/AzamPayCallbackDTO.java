package com.jemigraph.jemigraph_backend.DTO;

import java.util.Map;
import lombok.Data;

@Data
public class AzamPayCallbackDTO {
  private String message;
  private String user;
  private String password;
  private String clientId;
  private String transactionstatus;
  private String operator;
  private String reference;
  private String externalreference;
  private String utilityref;
  private String amount;
  private String transid;
  private String msisdn;
  private String mnoreference;
  private String submerchantAcc;
  private Map<String, Object> additionalProperties;
  private String signature;
}
