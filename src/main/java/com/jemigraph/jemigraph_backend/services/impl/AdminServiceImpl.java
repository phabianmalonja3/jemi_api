package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.DTO.TransactionAdminDTO;
import com.jemigraph.jemigraph_backend.DTO.UserDTO;
import com.jemigraph.jemigraph_backend.Entities.Subscription;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.Entities.Wallet;
import com.jemigraph.jemigraph_backend.enums.UserRole;
import com.jemigraph.jemigraph_backend.enums.WalletType;
import com.jemigraph.jemigraph_backend.events.PhotographerVerifiedEvent;
import com.jemigraph.jemigraph_backend.mappers.TransactionMapper;
import com.jemigraph.jemigraph_backend.repositories.SubscriptionRepository;
import com.jemigraph.jemigraph_backend.repositories.TransactionRepository;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.repositories.WalletRepository;
import com.jemigraph.jemigraph_backend.services.AdminService;
import com.jemigraph.jemigraph_backend.services.SmsService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

  private final UserRepository userRepository;
  private final TransactionRepository transactionRepository;
  private final TransactionMapper transactionMapper;
  private final WalletRepository walletRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final SmsService smsService;
  private final RedisTemplate<String, String> redisTemplate;

  @Override
  @Transactional(readOnly = true)
  public Page<TransactionAdminDTO> getAllTransactionsForAdmin(Pageable pageable) {

    return transactionRepository
        .findAllTransactions(pageable)
        .map(transactionMapper::toTransactionAdminDTO);
  }

  @Override
  public User accountSuspension(UUID uuid) {
    User user =
        userRepository.findById(uuid).orElseThrow(() -> new RuntimeException("User Not Found"));
    boolean newLockStatus = !user.isAccountNonLocked();
    user.setAccountNonLocked(newLockStatus);

    User savedUser = userRepository.save(user);

    String username = savedUser.getEmail();
    String lockKey = "account:blocked:" + username;

    if (!newLockStatus) {

      redisTemplate.opsForValue().set(lockKey, "ADMIN_LOCKED");
    } else {
      redisTemplate.delete(lockKey);
      redisTemplate.delete("login:attempts:" + username);
    }

    return savedUser;
  }

  @Override
  @Transactional(readOnly = true)
  public double getSystemBalance() {

    List<Wallet> systemWallets = walletRepository.findByType(WalletType.SYSTEM);

    if (systemWallets.isEmpty()) {
      return 0.0;
    }

    /*
     * TODO:
     * Calculate the actual system wallet balance.
     *
     * This depends on the fields available in Wallet.
     */

    return 0.0;
  }

  // ============================================================
  // FILTER USERS
  // ============================================================

  @Override
  @Transactional(readOnly = true)
  public Page<UserDTO> findFilteredUsers(String name, UserRole role, Pageable pageable) {

    Page<User> userPage;

    if (name != null && !name.isBlank() && role != null) {

      userPage = userRepository.findByNameContainingIgnoreCaseAndRole(name, role, pageable);

    } else if (role != null) {

      userPage = userRepository.findByRole(role, pageable);

    } else if (name != null && !name.isBlank()) {

      userPage = userRepository.findByNameContainingIgnoreCase(name, pageable);

    } else {

      userPage = userRepository.findAll(pageable);
    }

    return userPage.map(this::convertToDTO);
  }

  private UserDTO convertToDTO(User user) {
    UserDTO dto = new UserDTO();
    dto.setId(user.getId());
    dto.setName(user.getName());
    dto.setEmail(user.getEmail());

    dto.setRole(user.getRole() != null ? user.getRole().name() : null);

    dto.setIsVerified(user.isVerified());
    dto.setFcmToken(user.getFcmToken());
    dto.setBlocked(!user.isAccountNonLocked());
    Subscription subscription = subscriptionRepository.findByUserId(user.getId()).orElse(null);

    if (subscription != null) {

      LocalDateTime now = LocalDateTime.now();

      boolean active =
          subscription.getStatus()
                  == com.jemigraph.jemigraph_backend.enums.SubscriptionStatus.ACTIVE
              && subscription.getExpiresAt() != null
              && subscription.getExpiresAt().isAfter(now);

      dto.setSubscriptionStatus(
          active
              ? com.jemigraph.jemigraph_backend.enums.SubscriptionStatus.ACTIVE
              : subscription.getStatus());

      dto.setSubscriptionExpiresAt(subscription.getExpiresAt());

      dto.setTrialEndsAt(subscription.getExpiresAt());

    } else {

      dto.setSubscriptionStatus(null);
      dto.setSubscriptionExpiresAt(null);
      dto.setTrialEndsAt(null);
    }
    if (user.getUserProfile() != null) {

      dto.setPhone(user.getUserProfile().getPhone());

      dto.setBio(user.getUserProfile().getBio());

      dto.setDisplayName(user.getUserProfile().getDisplayName());
    }

    return dto;
  }

  @Override
  @Transactional(readOnly = true)
  public List<TransactionAdminDTO> getRecentTransactions(Pageable pageable) {

    return transactionRepository
        .findAll(pageable)
        .map(transactionMapper::toTransactionAdminDTO)
        .getContent();
  }

  // ============================================================
  // VERIFY PHOTOGRAPHER
  // ============================================================

  @Override
  public User verifyPhotographer(UUID userId) {

    User user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    user.setVerified(true);

    User verifiedUser = userRepository.save(user);

    eventPublisher.publishEvent(new PhotographerVerifiedEvent(verifiedUser));

    return verifiedUser;
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserDTO> getUnverifiedPhotographers() {

    return userRepository.findByRoleAndIsVerified(UserRole.PHOTOGRAPHER, false).stream()
        .map(
            user ->
                UserDTO.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .build())
        .toList();
  }

  // ============================================================
  // DELETE ACCOUNT
  // ============================================================

  @Override
  public void removeAccount(UUID userId) {

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("user is not found !"));

    userRepository.delete(user);
  }
}
