package com.jemigraph.jemigraph_backend.DTO;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WalletDTO {
private BigDecimal availableBalance;
    private BigDecimal lockedBalance;
    private String currency; // mfano: "TZS"
}