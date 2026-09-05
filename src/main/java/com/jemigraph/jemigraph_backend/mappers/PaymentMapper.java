package com.jemigraph.jemigraph_backend.mappers;


import com.jemigraph.jemigraph_backend.DTO.PaymentResponseDTO;
import com.jemigraph.jemigraph_backend.Entities.Payment;

public interface PaymentMapper {

    Payment toEntity ();
    PaymentResponseDTO toResponseDTO (Payment payment);
}
