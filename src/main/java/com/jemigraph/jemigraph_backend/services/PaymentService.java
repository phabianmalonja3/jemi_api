package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.DTO.PaymentRequestDTO;
import com.jemigraph.jemigraph_backend.DTO.WithdrawRequest;
import com.jemigraph.jemigraph_backend.Entities.Bookings;
import com.jemigraph.jemigraph_backend.enums.PaymentStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
@Service
public interface PaymentService {

     void updatePaymentStatus(UUID bookingId, BigDecimal request);
      void withdrawFunds(BigDecimal amount ,String phoneNumber ,String email);

}
