package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.DTO.*;
import com.jemigraph.jemigraph_backend.Entities.OtpVerification;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.Entities.UserDevice;
import com.jemigraph.jemigraph_backend.Entities.UserProfile;
import com.jemigraph.jemigraph_backend.enums.SubscriptionStatus;
import com.jemigraph.jemigraph_backend.enums.UserRole;
import com.jemigraph.jemigraph_backend.exceptions.PendingVerificationException;
import com.jemigraph.jemigraph_backend.exceptions.SuperAdminException;
import com.jemigraph.jemigraph_backend.exceptions.UserAlreadyExistsException;
import com.jemigraph.jemigraph_backend.mappers.RegistrationMapper;
import com.jemigraph.jemigraph_backend.mappers.UserMapper;
import com.jemigraph.jemigraph_backend.repositories.*;
import com.jemigraph.jemigraph_backend.requests.AuthenticationRequest;
import com.jemigraph.jemigraph_backend.services.AuthentificationService;
import com.jemigraph.jemigraph_backend.services.JwtService;
import com.jemigraph.jemigraph_backend.services.LoginAttemptService;
import com.jemigraph.jemigraph_backend.services.SessionService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthentificationService {

  private final UserRepository userRepository;
  private final UserProfileRepository userProfileRepository;
  private final JwtService jwtService;
  private final LoginAttemptService loginAttemptService;
  private final AuthenticationManager authenticationManager;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final EmailServiceImpl emailService;
  private final RegistrationMapper registrationMapper;
  private final OtpRepository otpRepository;
  private final UserDeviceRepository userDeviceRepository;
  private final SessionService sessionService;

  @Override
  @Transactional
  public AuthResponse authenticate(AuthenticationRequest request, String deviceName) {
    String email = request.email();

    if (loginAttemptService.isBlocked(email)) {
      throw new LockedException("Too many login attempts. Account locked for 15 minutes.");
    }

    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(email, request.password()));
    } catch (Exception e) {
      loginAttemptService.loginFailed(email);
      throw e;
    }

    loginAttemptService.loginSucceeded(email);

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    if (user.getRole() == UserRole.PHOTOGRAPHER && !user.isVerified()) {
      throw new PendingVerificationException("ACCOUNT_PENDING_VERIFICATION");
    }

    String clientDeviceName =
        (deviceName != null && !deviceName.isEmpty()) ? deviceName : "Unknown Device";

    if (request.fcmToken() != null) {
      user.setFcmToken(request.fcmToken());
      userRepository.save(user);
    }

    if (user.getRole() == UserRole.PHOTOGRAPHER) {
      String sessionId =
          sessionService.createSession(user.getId(), user.getEmail(), clientDeviceName);
      UserDevice newDevice = new UserDevice();
      newDevice.setUser(user);
      newDevice.setDeviceToken(sessionId);
      newDevice.setDeviceName(clientDeviceName);
      newDevice.setLastActiveAt(LocalDateTime.now());
      userDeviceRepository.save(newDevice);

      String jwtToken = jwtService.generateRefreshToken(email);
      String jwtRefreshToken = jwtService.generateRefreshToken(email);

      return AuthResponse.builder()
          .user(userMapper.toDto(user))
          .accessToken(jwtToken)
          .refreshToken(jwtRefreshToken)
          .build();
    } else if (user.getRole() == UserRole.ADMIN) {
      emailService.sendAdminOtp(user.getEmail());

      return AuthResponse.builder()
          .user(userMapper.toDto(user))
          .accessToken(null)
          .refreshToken(null)
          .build();
    }

    throw new RuntimeException("Unauthorized role access");
  }

  @Override
  public User getMe(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
  }

  @Override
  public boolean verifyOtp(String email, String userProvidedOtp) {
    Optional<OtpVerification> otpOpt = otpRepository.findTopByEmailOrderByIdDesc(email);

    if (otpOpt.isEmpty()) {
      return false;
    }
    OtpVerification otpData = otpOpt.get();
    if (!otpData.getOtpCode().equals(userProvidedOtp)) {
      return false;
    }
    if (LocalDateTime.now().isAfter(otpData.getExpiryTime())) {
      otpRepository.delete(otpData);
      return false;
    }
    otpRepository.delete(otpData);
    return true;
  }

  @Override
  @Transactional
  public RegistrationResponseDTO createUser(RegisterRequestDTO userDto) {

    if (userRepository.existsByEmail(userDto.getEmail())) {
      throw new UserAlreadyExistsException("Email Already Exist In Our System.");
    }

    if ("ADMIN".equals(userDto.getRole())) {
      throw new SuperAdminException("You can't register as Super Admin In Our System.");
    }
    UserRole userRole;
    try {
      userRole = UserRole.valueOf(userDto.getRole());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid role. Must be ADMIN, PHOTOGRAPHER, or CLIENT");
    }

    if (userRole == UserRole.PHOTOGRAPHER) {

      if (userDto.getPhone() == null || userDto.getPhone().isEmpty()) {
        throw new IllegalArgumentException(
            "Phone number is required for Photographer registration.");
      }

      if (userDto.getBio() == null || userDto.getBio().isEmpty()) {
        throw new IllegalArgumentException("Bio is required for Photographer registration.");
      }
    }

    boolean verifiedStatus = "CLIENT".equals(userDto.getRole());
    var userBuilder =
        User.builder()
            .name(userDto.getName())
            .email(userDto.getEmail())
            .password(passwordEncoder.encode(userDto.getPassword()))
            .role(userRole)
            .isVerified(verifiedStatus)
            .isBusy(false)
            .isOnline(false)
            .subscriptionStatus(SubscriptionStatus.INACTIVE);

    if (userRole == UserRole.PHOTOGRAPHER) {
      userBuilder
          .trialEndsAt(LocalDateTime.now().plusDays(30))
          .subscriptionStatus(SubscriptionStatus.TRIAL);
    }

    User createdUser = userBuilder.build();
    User savedUser = userRepository.save(createdUser);
    if (userRole == UserRole.PHOTOGRAPHER) {
      UserProfile userProfile =
          UserProfile.builder()
              .user(savedUser)
              .displayName(
                  userDto.getDisplayName() != null && !userDto.getDisplayName().isEmpty()
                      ? userDto.getDisplayName()
                      : userDto.getName())
              .phone(userDto.getPhone())
              .bio(userDto.getBio())
              .build();

      UserProfile savedProfile = userProfileRepository.save(userProfile);
      savedUser.setUserProfile(savedProfile);

    } else if (userRole == UserRole.CLIENT
        && userDto.getPhone() != null
        && !userDto.getPhone().isEmpty()) {

      UserProfile userProfile =
          UserProfile.builder()
              .user(savedUser)
              .displayName(
                  userDto.getDisplayName() != null && !userDto.getDisplayName().isEmpty()
                      ? userDto.getDisplayName()
                      : userDto.getName())
              .phone(userDto.getPhone())
              .bio(userDto.getBio())
              .build();

      UserProfile savedProfile = userProfileRepository.save(userProfile);
      savedUser.setUserProfile(savedProfile);
    }
    return registrationMapper.toDto(savedUser);
  }

  @Override
  @Transactional
  public boolean requestPasswordReset(String email) {
    try {
      emailService.sendForgotPassword(email);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Scheduled(fixedRate = 3600000)
  public void cleanupExpiredOpts() {
    otpRepository.deleteByExpiryTimeBefore(LocalDateTime.now());
  }

  @Override
  public boolean completePasswordReset(String email, String newPassword) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    try {
      user.setPassword(passwordEncoder.encode(newPassword));
      user.setResetToken(null);
      user.setResetTokenExpiry(null);
      userRepository.saveAndFlush(user);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Transactional
  @Override
  public UserDTO changeUserPassword(String email, String currentPassword, String newPassword) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
      throw new RuntimeException("Current password is incorrect");
    }
    String encryptedPassword = passwordEncoder.encode(newPassword);
    user.setPassword(encryptedPassword);
    User updatedUser = userRepository.save(user);

    return userMapper.toDto(updatedUser);
  }

  @Override
  @Transactional
  public AuthResponse verifyAdminOtp(OtpVerificationRequestDTO request, String deviceName) {
    String email = request.getEmail();
    String code = request.getCode();

    Optional<OtpVerification> otpOpt = otpRepository.findTopByEmailOrderByIdDesc(email);
    if (otpOpt.isEmpty()) {
      throw new RuntimeException("Invalid or expired OTP code");
    }

    OtpVerification otpData = otpOpt.get();
    if (!otpData.getOtpCode().equals(code)) {
      throw new RuntimeException("Invalid verification code");
    }

    if (LocalDateTime.now().isAfter(otpData.getExpiryTime())) {
      otpRepository.delete(otpData);
      throw new RuntimeException("OTP code has expired. Please request a new one.");
    }

    otpRepository.delete(otpData);

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    if (user.getRole() != UserRole.ADMIN) {
      throw new RuntimeException("Unauthorized access");
    }

    String clientDeviceName =
        (deviceName != null && !deviceName.isEmpty()) ? deviceName : "Unknown Device";

    String sessionId =
        sessionService.createSession(user.getId(), user.getEmail(), clientDeviceName);

    UserDevice newDevice = new UserDevice();
    newDevice.setUser(user);
    newDevice.setDeviceToken(sessionId);
    newDevice.setDeviceName(clientDeviceName);
    newDevice.setLastActiveAt(LocalDateTime.now());
    userDeviceRepository.save(newDevice);

    String jwtToken = jwtService.generateRefreshToken(email);
    String jwtRefreshToken = jwtService.generateRefreshToken(email);

    return AuthResponse.builder()
        .user(userMapper.toDto(user))
        .accessToken(jwtToken)
        .refreshToken(jwtRefreshToken)
        .build();
  }
}
