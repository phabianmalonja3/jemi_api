package com.jemigraph.jemigraph_backend.controllers;

import com.jemigraph.jemigraph_backend.DTO.*;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.mappers.UserResponseMapper;
import com.jemigraph.jemigraph_backend.models.ResetPasswordRequest;
import com.jemigraph.jemigraph_backend.repositories.OtpRepository;
import com.jemigraph.jemigraph_backend.repositories.UserDeviceRepository;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.requests.AuthenticationRequest;
import com.jemigraph.jemigraph_backend.services.SessionService;
import com.jemigraph.jemigraph_backend.services.impl.AuthenticationServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("auth")
@AllArgsConstructor
public class AuthentificationController {
  private final AuthenticationServiceImpl authentificationService;
  private final UserResponseMapper userResponseMapper;
  private final UserRepository userRepository;
  private final UserDeviceRepository userDeviceRepository;
  private final SessionService sessionService;
  private final OtpRepository otpRepository;

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> authenticate(
      @NonNull @Valid @RequestBody AuthenticationRequest request,
      @RequestHeader(value = "X-Device-Name", required = false) String deviceName,
      @RequestHeader(value = "User-Agent", defaultValue = "Web Browser") String userAgent) {
    String finalDeviceName = (deviceName != null && !deviceName.isBlank()) ? deviceName : userAgent;
    return ResponseEntity.ok(authentificationService.authenticate(request, finalDeviceName));
  }

  @PostMapping("/verify-admin-otp")
  public ResponseEntity<AuthResponse> verifyAdminOtp(
          @RequestBody OtpVerificationRequestDTO request,
          HttpServletRequest servletRequest) {
    String clientDeviceName = servletRequest.getHeader("User-Agent");

    AuthResponse response = authentificationService.verifyAdminOtp(request, clientDeviceName);

    return ResponseEntity.ok(response);
  }
  @PostMapping("/register")
  public ResponseEntity<RegistrationResponseDTO> register(
      @NonNull @Valid @RequestBody RegisterRequestDTO userDTO) {

    return new ResponseEntity<>(
        authentificationService.createUser(userDTO),
        HttpStatusCode.valueOf(org.apache.http.HttpStatus.SC_CREATED));
  }

  @GetMapping("/me")
  public ResponseEntity<?> getCurrentUser(Principal principal) {
    return ResponseEntity.ok(
        userResponseMapper.toDto(authentificationService.getMe(principal.getName())));
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<Map<String, String>> forgotPassword(
      @Valid @RequestBody PasswordForgotDTO request) {
    authentificationService.requestPasswordReset(request.getEmail());
    Map<String, String> response = new HashMap<>();
    response.put("message", "We have sent the Otp Link on your email !.");
    return ResponseEntity.ok(response);
  }

  @PostMapping("/otp-validate")
  public ResponseEntity<Map<String, String>> otpValidate(
      @Valid @RequestBody OtpRequestDTO request) {
    boolean isValid = authentificationService.verifyOtp(request.getEmail(), request.getOtp());
    Map<String, String> response = new HashMap<>();

    if (!isValid) {
      response.put("message", "Invalid or expired OTP");

      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    } else {
      response.put("message", "OTP verified successfully. Please enter your new password.");

      return ResponseEntity.ok(response);
    }
  }

  @Transactional
  @PostMapping("/logout-all")
  public ResponseEntity<?> logoutAllDevices(Principal principal, HttpServletResponse response) {

    User user =
        userRepository
            .findByEmail(principal.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

    sessionService.invalidateSession(user.getId());

    userDeviceRepository.deleteByUserId(user.getId());

    user.setTokenVersion(user.getTokenVersion() + 1);

    userRepository.save(user);

    Cookie cookie = new Cookie("token", null);

    cookie.setHttpOnly(true);
    cookie.setSecure(false);

    cookie.setPath("/");
    cookie.setMaxAge(0);

    response.addCookie(cookie);

    return ResponseEntity.ok(Map.of("message", "Logged out from all devices successfully."));
  }

  @PostMapping("/reset-password")
  public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    boolean isSuccess =
        authentificationService.completePasswordReset(request.getEmail(), request.getNewPassword());
    if (!isSuccess) {
      return ResponseEntity.badRequest().body(Map.of("message", "Invalid Token."));
    }
    return ResponseEntity.ok(Map.of("message", "password short length."));
  }

  @PostMapping("/change-password")
  public ResponseEntity<?> updatePassword(
      Principal principal, @Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
    authentificationService.changeUserPassword(
        principal.getName(),
        passwordUpdateDTO.getOldPassword(),
        passwordUpdateDTO.getNewPassword());
    return ResponseEntity.ok(Map.of("success", true, "message", "Password updated successfully."));
  }
}
