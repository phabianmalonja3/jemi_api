package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.DTO.MnoCallbackDTO;
import com.jemigraph.jemigraph_backend.DTO.MnoCheckoutResponse;
import com.jemigraph.jemigraph_backend.Entities.User;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AzamPesaService {

  MnoCheckoutResponse mnoCheckout(User user, UUID planId, String phoneNumber, String provider);

  void processCallback(MnoCallbackDTO callbackPayload);

  List<Map<String, Object>> getPaymentPartners();
}
