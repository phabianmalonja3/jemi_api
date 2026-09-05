package com.jemigraph.jemigraph_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PinVerificationResult {
    private boolean valid;
    private boolean isPinChanged;
}