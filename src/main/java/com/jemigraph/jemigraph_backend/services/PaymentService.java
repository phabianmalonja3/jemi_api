package com.jemigraph.jemigraph_backend.services;


import java.math.BigDecimal;
import java.util.UUID;
//@Service

public interface PaymentService {

     void updatePaymentStatus(UUID bookingId, BigDecimal request);
      void withdrawFunds(BigDecimal amount ,String phoneNumber ,String email);

}
