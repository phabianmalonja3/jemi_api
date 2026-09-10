package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.DTO.PaymentCallbackDto;
import com.jemigraph.jemigraph_backend.DTO.PaymentInitiationResponse;
import com.jemigraph.jemigraph_backend.DTO.SubscriberResponseDto;
import com.jemigraph.jemigraph_backend.DTO.SubscriptionPaymentResponseDTO;
import com.jemigraph.jemigraph_backend.Entities.Payment;
import com.jemigraph.jemigraph_backend.Entities.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public interface PaymentSystemService {

  PaymentInitiationResponse initiatePaymentAsync(User user, UUID planId, String phoneNumber);

  boolean handleCallback(PaymentCallbackDto callbackPayload);

  SubscriptionPaymentResponseDTO getPaymentStatusResponse(String orderId);

  List<SubscriberResponseDto> getAllSubscribers();

  byte[] generateReceiptPdf(String orderId) throws Exception;

  List<Payment> getPaymentsByUserAndDateRange(UUID userId, LocalDateTime start, LocalDateTime end);

  byte[] generateBulkReceiptPdf(List<Payment> payments, User user);

  Payment getPaymentByIdAndUser(UUID id);
}
