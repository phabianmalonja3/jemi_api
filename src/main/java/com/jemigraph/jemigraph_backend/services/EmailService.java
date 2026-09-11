package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.events.PhotographerVerifiedEvent;
import org.springframework.stereotype.Service;

@Service
public interface EmailService {
 void sendBookingConfirmation(String toEmail, String clientName, String photographerName, String date);
 void  sendPaymentRequest(String toEmail, String clientName, String amount, String paymentUrl);
 void  sendPaymentRequest();
 void sendVerification(PhotographerVerifiedEvent event);
 void sendForgotPassword(String toEmail);
 void sendPasswordResetEmail(String toEmail, String resetLink);
 void sendSubscriptionActivated(
         String toEmail,
         String userName,
         String planName,
         String amount,
         String startDate,
         String expiryDate);

    void sendEmailToAdmin(String s);

}
