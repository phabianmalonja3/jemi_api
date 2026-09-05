package com.jemigraph.jemigraph_backend.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonikerPaymentRequest {
    private String order_id;
    private double amount;
    private String buyer_phone;
    private String buyer_name;
    private String buyer_email;
    private String fee_payer;
    private Object metadata;
}