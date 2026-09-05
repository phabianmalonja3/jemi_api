package com.jemigraph.jemigraph_backend.DTO;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingPaymentRequestDTO {
    private UUID photographerId;
    @NotNull(message = "Kiasi ni lazima")
    @DecimalMin(value = "0.01", message = "Malipo hayawezi kuwa chini ya 0.01")
    private BigDecimal amount;
    private UUID bookingId;
}