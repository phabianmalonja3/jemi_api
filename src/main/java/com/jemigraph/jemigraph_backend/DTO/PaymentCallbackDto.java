package com.jemigraph.jemigraph_backend.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentCallbackDto {

    @JsonProperty("transactionNumber")
    private String transactionNumber;

    @JsonProperty("status")
    private String status;

    @JsonProperty("referenceNumber")
    private Object referenceNumber;

    @JsonProperty("receiptNumber")
    private Object receiptNumber;

    @JsonProperty("provider")
    private Object provider;

    @JsonProperty("amount")
    private Object amount;

    @JsonProperty("phone")
    private Object phone;
}