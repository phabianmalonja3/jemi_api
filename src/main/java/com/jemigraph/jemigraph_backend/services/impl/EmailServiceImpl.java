package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.Entities.OtpVerification;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.repositories.OtpRepository;
import com.jemigraph.jemigraph_backend.events.PhotographerVerifiedEvent;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final OtpRepository otpRepository;

    @Override
    public void sendBookingConfirmation(String toEmail, String clientName, String photographerName, String date) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@jemigraph.com");
        message.setTo(toEmail);
        message.setSubject("Booking Request Received - Jemigraph");
        message.setText("Hi " + clientName + ",\n\n" +
                "Your booking request for " + photographerName + " on " + date + " has been received.\n" +
                "The photographer will contact you shortly to confirm.\n\n" +
                "Thank you for choosing Jemigraph!");

        mailSender.send(message);
    }

    @Async
    @Override
    public void sendPaymentRequest(String toEmail, String clientName, String amount, String paymentUrl) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("noreply@jemigraph.com");
            helper.setTo(toEmail);
            helper.setSubject("Secure Your Booking - Jemigraph");

            String htmlContent = "<html><body style='font-family: Arial, sans-serif;'>" +
                    "<h2>Hi " + clientName + ",</h2>" +
                    "<p>Great news! Your booking has been <b>Accepted</b>.</p>" +
                    "<p>To secure your session, please pay the 40% deposit of <b>TSh " + amount + "</b>.</p>" +
                    "<div style='margin-top: 20px;'>" +
                    "  <a href='" + paymentUrl + "' style='background-color: #007bff; color: white; padding: 12px 20px; text-decoration: none; border-radius: 5px; display: inline-block;'>PAY DEPOSIT NOW</a>" +
                    "</div>" +
                    "<p style='margin-top: 20px; font-size: 0.9em; color: #555;'>This link will redirect you to our secure payment gateway.</p>" +
                    "</body></html>";

            helper.setText(htmlContent, true); // 'true' enables HTML
            mailSender.send(message);
        } catch (MessagingException e) {
            // Log the error in production
            System.err.println("Failed to send email: " + e.getMessage());
        }


    }

    @Override
    public void sendPaymentRequest() {

    }

    @Override
    @Async
    public void sendVerification(PhotographerVerifiedEvent event) {
        User user = event.user();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("noreply@jemigraph.co.tz");
            helper.setTo(user.getEmail());
            helper.setSubject("Account Verified - Welcome to Jemigraph!");
            String htmlContent = "<html><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                    "<div style='max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>" +
                    "<h2 style='color: #25632D;'>Account Verified!</h2>" +
                    "<p>Hello <b>" + user.getName() + "</b>,</p>" +
                    "<p>Congratulations! We are pleased to inform you that your account has been successfully verified.</p>" +
                    "<p>You can now log in to the Jemigraph platform, manage your profile, and start receiving booking requests from clients.</p>" +
                    "<div style='margin: 30px 0; text-align: center;'>" +
                    "</div>" +
                    "<p>If you have any questions, feel free to contact our support team.</p>" +
                    "<p>Best regards,<br><b>The Jemigraph Team</b></p>" +
                    "</div></body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
          log.info("📧 Verification email successfully sent to: {}", user.getEmail());

        } catch (MessagingException e) {
            log.error("❌ Failed to send verification email to {}: {}", user.getEmail(), e.getMessage());
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Async
    @Override
    public void sendForgotPassword(String toEmail) {

        User user = userRepository.findByEmail(toEmail)
                .orElseThrow(() -> new RuntimeException("User Does not Exist !"));

        String otp = generateRandom6DigitCode();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);
        OtpVerification otpEntity = OtpVerification.builder()
                .email(toEmail).otpCode(otp).expiryTime(expiry).build();

        otpRepository.save(otpEntity);


        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("noreply@jemigraph.co.tz");
            helper.setTo(toEmail);
            helper.setSubject("Password Reset OTP - Jemigraph");

            String htmlContent = "<html><body style='font-family: Arial, sans-serif;'>" +
                    "<h2>Hello " + user.getName() + ",</h2>" +
                    "<p>We received a request to reset your password. Use the code below to proceed.</p>" +
                    "<h1 style='color: #007bff; letter-spacing: 5px;'>" + otp + "</h1>" +
                    "<p>This code <b>expires in 10 minutes</b>.</p>" +
                    "<p>If you did not request this, please ignore this email.</p>" +
                    "</body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String generateRandom6DigitCode() {

        SecureRandom random = new SecureRandom();
        int code = random.nextInt(1000000);
        return String.format("%06d", code);
    }

    @Async
    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("noreply@jemigraph.com");
            helper.setTo(toEmail);
            helper.setSubject("Reset Your Jemigraph Password");

            String htmlContent = "<html><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                    "<h2>Password Reset Request</h2>" +
                    "<p>Hello,</p>" +
                    "<p>We received a request to reset the password for your Jemigraph account. Click the button below to choose a new one:</p>" +
                    "<div style='margin: 30px 0;'>" +
                    "  <a href='" + resetLink + "' style='background-color: #28a745; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;'>Reset Password</a>" +
                    "</div>" +
                    "<p><b>Note:</b> This link will expire in 15 minutes.</p>" +
                    "<p>If you didn't request this, you can safely ignore this email.</p>" +
                    "<br>" +
                    "<p>Best regards,<br>The Jemigraph Team</p>" +
                    "</body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            // In a real app, use a proper Logger (e.g., SLF4J)
            System.err.println("Failed to send password reset email to " + toEmail + ": " + e.getMessage());
        }
    }

    @Async
    @Override
    public void sendEmailToAdmin(String s) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("noreply@jemigraph.com");
            helper.setTo("phabianmalonja3@gmial.com");
            helper.setSubject("Jemigraph: New Notification");
            String messageContent ="Login To get More detail";
            String htmlContent = "<h2>Jemigraph Notification</h2>";
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Imeshindwa kutuma email: " + e.getMessage());
        }
    }



}
