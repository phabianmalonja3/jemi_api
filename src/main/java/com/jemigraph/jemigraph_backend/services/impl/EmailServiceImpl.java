package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.Entities.OtpVerification;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.events.PhotographerVerifiedEvent;
import com.jemigraph.jemigraph_backend.repositories.OtpRepository;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.services.EmailService;
import com.jemigraph.jemigraph_backend.services.SmsService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

  private static final String FROM_EMAIL = "help@jemigraph.co.tz";
  private static final String FROM_NAME = "Jemigraph";

  private final JavaMailSender mailSender;
  private final UserRepository userRepository;
  private final OtpRepository otpRepository;
  private final SmsService smsService;

  /** Send booking confirmation email. */
  @Override
  public void sendBookingConfirmation(
      String toEmail, String clientName, String photographerName, String date) {

    SimpleMailMessage message = new SimpleMailMessage();

    // SimpleMailMessage accepts only the email address.
    message.setFrom(FROM_EMAIL);

    message.setTo(toEmail);
    message.setSubject("Booking Request Received - Jemigraph");

    message.setText(
        "Hi "
            + clientName
            + ",\n\n"
            + "Your booking request for "
            + photographerName
            + " on "
            + date
            + " has been received.\n"
            + "The photographer will contact you shortly to confirm.\n\n"
            + "Thank you for choosing Jemigraph!\n\n"
            + "Best regards,\n"
            + "The Jemigraph Team");

    mailSender.send(message);
  }

  /** Send payment request email. */
  @Async
  @Override
  public void sendPaymentRequest(
      String toEmail, String clientName, String amount, String paymentUrl) {

    try {
      MimeMessage message = mailSender.createMimeMessage();

      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(FROM_EMAIL, FROM_NAME);
      helper.setTo(toEmail);
      helper.setSubject("Secure Your Booking - Jemigraph");

      String htmlContent =
          "<html>"
              + "<body style='font-family: Arial, sans-serif;'>"
              + "<h2>Hi "
              + clientName
              + ",</h2>"
              + "<p>Great news! Your booking has been <b>Accepted</b>.</p>"
              + "<p>"
              + "To secure your session, please pay the 40% deposit of "
              + "<b>TSh "
              + amount
              + "</b>."
              + "</p>"
              + "<div style='margin-top: 20px;'>"
              + "<a href='"
              + paymentUrl
              + "' "
              + "style='background-color: #007bff; "
              + "color: white; "
              + "padding: 12px 20px; "
              + "text-decoration: none; "
              + "border-radius: 5px; "
              + "display: inline-block;'>"
              + "PAY DEPOSIT NOW"
              + "</a>"
              + "</div>"
              + "<p style='margin-top: 20px; font-size: 0.9em; color: #555;'>"
              + "This link will redirect you to our secure payment gateway."
              + "</p>"
              + "</body>"
              + "</html>";

      helper.setText(htmlContent, true);

      mailSender.send(message);

      log.info("Payment request email sent to {}", toEmail);

    } catch (MessagingException e) {
      log.error("Failed to send payment request email to {}: {}", toEmail, e.getMessage(), e);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  /** Required by EmailService interface. */
  @Override
  public void sendPaymentRequest() {
    // Intentionally empty.
  }

  /** Send photographer verification email. */
  @Async
  @Override
  public void sendVerification(PhotographerVerifiedEvent event) {

    User user = event.user();

    try {
      MimeMessage message = mailSender.createMimeMessage();

      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(FROM_EMAIL, FROM_NAME);
      helper.setTo(user.getEmail());
      helper.setSubject("Account Verified - Welcome to Jemigraph!");

      String htmlContent =
          "<html>"
              + "<body style='font-family: Arial, sans-serif; "
              + "line-height: 1.6; color: #333;'>"
              + "<div style='max-width: 600px; "
              + "margin: auto; "
              + "padding: 20px; "
              + "border: 1px solid #ddd; "
              + "border-radius: 10px;'>"
              + "<h2 style='color: #25632D;'>"
              + "Account Verified!"
              + "</h2>"
              + "<p>Hello <b>"
              + user.getName()
              + "</b>,</p>"
              + "<p>"
              + "Congratulations! We are pleased to inform you "
              + "that your account has been successfully verified."
              + "</p>"
              + "<p>"
              + "You can now log in to the Jemigraph platform, "
              + "manage your profile, and start receiving booking "
              + "requests from clients."
              + "</p>"
              + "<p>"
              + "If you have any questions, feel free to contact "
              + "our support team."
              + "</p>"
              + "<p>"
              + "Best regards,<br>"
              + "<b>The Jemigraph Team</b>"
              + "</p>"
              + "</div>"
              + "</body>"
              + "</html>";

      helper.setText(htmlContent, true);

      mailSender.send(message);

      log.info("Verification email successfully sent to: {}", user.getEmail());

    } catch (MessagingException | UnsupportedEncodingException e) {

      log.error("Failed to send verification email to {}: {}", user.getEmail(), e.getMessage(), e);

      throw new RuntimeException("Failed to send verification email", e);
    }
  }

  /** Send forgot-password OTP through email and SMS. */
  @Async
  @Override
  @Transactional
  public void sendForgotPassword(String toEmail) {

    User user =
        userRepository
            .findFirstByEmail(toEmail)
            .orElseThrow(() -> new RuntimeException("User Does not Exist !"));

    String otp = generateRandom6DigitCode();

    LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);

    otpRepository.deleteByEmail(toEmail);

    OtpVerification otpEntity =
        OtpVerification.builder().email(toEmail).otpCode(otp).expiryTime(expiry).build();

    otpRepository.save(otpEntity);

    log.info("🔐 NEW PASSWORD RESET OTP GENERATED");
    log.info("📧 Email: {}", toEmail);
    log.info("🔢 OTP: {}", otp);
    log.info("⏰ Expiry: {}", expiry);

    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom(FROM_EMAIL, FROM_NAME);
      helper.setTo(toEmail);
      helper.setSubject("Password Reset OTP - Jemigraph");
      String htmlContent =
          "<div style='background-color: #f4f6f9; "
              + "padding: 30px 0; "
              + "font-family: Arial, sans-serif;'>"
              + "<div style='max-width: 500px; "
              + "margin: 0 auto; "
              + "background: #ffffff; "
              + "border-radius: 8px; "
              + "overflow: hidden; "
              + "box-shadow: 0 4px 12px rgba(0,0,0,0.05);'>"
              + "<div style='background: #0f172a; "
              + "padding: 20px; "
              + "text-align: center; "
              + "color: #ffffff;'>"
              + "<h2 style='margin: 0; "
              + "font-size: 22px; "
              + "letter-spacing: 1px;'>"
              + "JEMIGRAPH"
              + "</h2>"
              + "</div>"
              + "<div style='padding: 30px; "
              + "color: #334155;'>"
              + "<h3 style='margin-top: 0; "
              + "color: #0f172a;'>"
              + "Hello "
              + user.getName()
              + ","
              + "</h3>"
              + "<p style='line-height: 1.6; "
              + "font-size: 15px;'>"
              + "We received a request to reset your password "
              + "for your Jemigraph account. "
              + "Use the verification code below to proceed:"
              + "</p>"
              + "<div style='text-align: center; "
              + "margin: 30px 0;'>"
              + "<span style='display: inline-block; "
              + "background: #f8fafc; "
              + "border: 2px dashed #cbd5e1; "
              + "color: #0f172a; "
              + "font-size: 32px; "
              + "font-weight: bold; "
              + "letter-spacing: 6px; "
              + "padding: 12px 24px; "
              + "border-radius: 6px;'>"
              + otp
              + "</span>"
              + "</div>"
              + "<p style='font-size: 14px; "
              + "color: #64748b; "
              + "text-align: center;'>"
              + "This code <b>expires in 10 minutes</b>."
              + "</p>"
              + "<hr style='border: none; "
              + "border-top: 1px solid #e2e8f0; "
              + "margin: 25px 0;'>"
              + "<p style='font-size: 13px; "
              + "color: #94a3b8; "
              + "line-height: 1.4; "
              + "margin-bottom: 0;'>"
              + "If you did not request a password reset, "
              + "please ignore this email or contact support "
              + "if you have concerns."
              + "</p>"
              + "</div>"
              + "<div style='background: #f8fafc; "
              + "padding: 15px; "
              + "text-align: center; "
              + "font-size: 12px; "
              + "color: #94a3b8; "
              + "border-top: 1px solid #e2e8f0;'>"
              + "&copy; 2026 Jemigraph. All rights reserved."
              + "</div>"
              + "</div>"
              + "</div>";

      helper.setText(htmlContent, true);

      mailSender.send(message);

      // Send OTP through SMS as well.
      String phone = user.getUserProfile() != null ? user.getUserProfile().getPhone() : null;

      if (phone != null && !phone.isBlank()) {

        String smsMessage =
            "Your Jemigraph password reset OTP is: " + otp + ". Valid for 10 minutes.";

        smsService.sendSms(phone, smsMessage);
      }

      log.info("Password reset OTP sent to {}", toEmail);

    } catch (MessagingException e) {

      log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage(), e);

      throw new RuntimeException("Failed to send email", e);

    } catch (Exception e) {

      log.error("Failed to send password reset notification to {}: {}", toEmail, e.getMessage(), e);

      throw new RuntimeException(
          "Failed to send password reset notification: " + e.getMessage(), e);
    }
  }

  /** Generate six-digit OTP. */
  private String generateRandom6DigitCode() {

    SecureRandom random = new SecureRandom();

    int code = random.nextInt(1_000_000);

    return String.format("%06d", code);
  }

  /** Send password reset link email. */
  @Async
  @Override
  public void sendPasswordResetEmail(String toEmail, String resetLink) {

    try {
      MimeMessage message = mailSender.createMimeMessage();

      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(FROM_EMAIL, FROM_NAME);

      helper.setTo(toEmail);

      helper.setSubject("Reset Your Jemigraph Password");

      String htmlContent =
          "<html>"
              + "<body style='font-family: Arial, sans-serif; "
              + "line-height: 1.6; color: #333;'>"
              + "<h2>Password Reset Request</h2>"
              + "<p>Hello,</p>"
              + "<p>"
              + "We received a request to reset the password "
              + "for your Jemigraph account. "
              + "Click the button below to choose a new one:"
              + "</p>"
              + "<div style='margin: 30px 0;'>"
              + "<a href='"
              + resetLink
              + "' "
              + "style='background-color: #28a745; "
              + "color: white; "
              + "padding: 12px 25px; "
              + "text-decoration: none; "
              + "border-radius: 5px; "
              + "font-weight: bold;'>"
              + "Reset Password"
              + "</a>"
              + "</div>"
              + "<p>"
              + "<b>Note:</b> "
              + "This link will expire in 15 minutes."
              + "</p>"
              + "<p>"
              + "If you didn't request this, "
              + "you can safely ignore this email."
              + "</p>"
              + "<br>"
              + "<p>"
              + "Best regards,<br>"
              + "The Jemigraph Team"
              + "</p>"
              + "</body>"
              + "</html>";

      helper.setText(htmlContent, true);

      mailSender.send(message);

      log.info("Password reset email sent to {}", toEmail);

    } catch (MessagingException e) {

      log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage(), e);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  /** Send notification email to admin. */
  @Async
  @Override
  public void sendEmailToAdmin(String s) {

    try {
      MimeMessage message = mailSender.createMimeMessage();

      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(FROM_EMAIL, FROM_NAME);

      // Send admin notification to the support mailbox.
      helper.setTo(FROM_EMAIL);

      helper.setSubject("Jemigraph: New Notification");

      String htmlContent = "<h2>Jemigraph Notification</h2>" + "<p>Login to get more details.</p>";

      helper.setText(htmlContent, true);

      mailSender.send(message);

      log.info("Admin notification email sent to {}", FROM_EMAIL);

    } catch (Exception e) {

      log.error("Failed to send admin notification email: {}", e.getMessage(), e);
    }
  }

  @Async
  @Override
  public void sendSubscriptionActivated(
      String toEmail,
      String userName,
      String planName,
      String amount,
      String startDate,
      String expiryDate) {

    try {
      MimeMessage message = mailSender.createMimeMessage();

      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(FROM_EMAIL, FROM_NAME);
      helper.setTo(toEmail);
      helper.setSubject("Subscription Activated - Jemigraph");

      String htmlContent =
          "<html>"
              + "<body style='margin:0; padding:0; "
              + "background-color:#f4f6f9; "
              + "font-family:Arial,sans-serif;'>"
              + "<div style='max-width:600px; "
              + "margin:30px auto; "
              + "background:#ffffff; "
              + "border-radius:10px; "
              + "overflow:hidden; "
              + "box-shadow:0 4px 15px rgba(0,0,0,0.08);'>"

              // Header
              + "<div style='background:#25632D; "
              + "padding:25px; "
              + "text-align:center; "
              + "color:#ffffff;'>"
              + "<h1 style='margin:0; "
              + "font-size:26px;'>"
              + "JEMIGRAPH"
              + "</h1>"
              + "<p style='margin:8px 0 0; "
              + "font-size:14px;'>"
              + "Subscription Activated"
              + "</p>"
              + "</div>"

              // Body
              + "<div style='padding:30px;'>"
              + "<h2 style='color:#1f2937; "
              + "margin-top:0;'>"
              + "Hello "
              + userName
              + ","
              + "</h2>"
              + "<p style='font-size:15px; "
              + "line-height:1.7; "
              + "color:#4b5563;'>"
              + "Great news! Your Jemigraph subscription "
              + "has been successfully activated."
              + "</p>"

              // Success box
              + "<div style='background:#ecfdf5; "
              + "border:1px solid #a7f3d0; "
              + "border-radius:8px; "
              + "padding:20px; "
              + "margin:25px 0;'>"
              + "<h3 style='margin-top:0; "
              + "color:#166534;'>"
              + "✓ Subscription Active"
              + "</h3>"
              + "<p style='margin:8px 0; "
              + "color:#166534;'>"
              + "Your account now has active subscription access."
              + "</p>"
              + "</div>"

              // Subscription details
              + "<h3 style='color:#1f2937;'>"
              + "Subscription Details"
              + "</h3>"
              + "<table style='width:100%; "
              + "border-collapse:collapse; "
              + "font-size:14px;'>"
              + "<tr>"
              + "<td style='padding:10px 0; "
              + "border-bottom:1px solid #e5e7eb; "
              + "color:#6b7280;'>"
              + "Plan"
              + "</td>"
              + "<td style='padding:10px 0; "
              + "border-bottom:1px solid #e5e7eb; "
              + "font-weight:bold; "
              + "text-align:right;'>"
              + planName
              + "</td>"
              + "</tr>"
              + "<tr>"
              + "<td style='padding:10px 0; "
              + "border-bottom:1px solid #e5e7eb; "
              + "color:#6b7280;'>"
              + "Amount"
              + "</td>"
              + "<td style='padding:10px 0; "
              + "border-bottom:1px solid #e5e7eb; "
              + "font-weight:bold; "
              + "text-align:right;'>"
              + "TSh "
              + amount
              + "</td>"
              + "</tr>"
              + "<tr>"
              + "<td style='padding:10px 0; "
              + "border-bottom:1px solid #e5e7eb; "
              + "color:#6b7280;'>"
              + "Start Date"
              + "</td>"
              + "<td style='padding:10px 0; "
              + "border-bottom:1px solid #e5e7eb; "
              + "text-align:right;'>"
              + startDate
              + "</td>"
              + "</tr>"
              + "<tr>"
              + "<td style='padding:10px 0; "
              + "color:#6b7280;'>"
              + "Expiry Date"
              + "</td>"
              + "<td style='padding:10px 0; "
              + "font-weight:bold; "
              + "text-align:right;'>"
              + expiryDate
              + "</td>"
              + "</tr>"
              + "</table>"
              + "<p style='margin-top:25px; "
              + "font-size:14px; "
              + "line-height:1.6; "
              + "color:#4b5563;'>"
              + "You can now continue using Jemigraph and "
              + "enjoy all the features included in your subscription."
              + "</p>"
              + "</div>"

              // Footer
              + "<div style='background:#f8fafc; "
              + "padding:18px; "
              + "text-align:center; "
              + "font-size:12px; "
              + "color:#94a3b8; "
              + "border-top:1px solid #e5e7eb;'>"
              + "&copy; 2026 Jemigraph. All rights reserved."
              + "</div>"
              + "</div>"
              + "</body>"
              + "</html>";

      helper.setText(htmlContent, true);

      mailSender.send(message);

      log.info("Subscription activation email successfully sent to {}", toEmail);

    } catch (MessagingException e) {

      log.error(
          "Failed to send subscription activation email to {}: {}", toEmail, e.getMessage(), e);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
