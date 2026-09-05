package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.DTO.TransactionAdminDTO;
import com.jemigraph.jemigraph_backend.DTO.UserDTO;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.Entities.Wallet;
import com.jemigraph.jemigraph_backend.enums.UserRole;
import com.jemigraph.jemigraph_backend.enums.WalletType;
import com.jemigraph.jemigraph_backend.events.PhotographerVerifiedEvent;
import com.jemigraph.jemigraph_backend.mappers.TransactionMapper;
import com.jemigraph.jemigraph_backend.repositories.TransactionRepository;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.repositories.WalletRepository;
import com.jemigraph.jemigraph_backend.services.AdminService;
import com.jemigraph.jemigraph_backend.services.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final WalletRepository walletRepository; 
    private final ApplicationEventPublisher eventPublisher;
    private final SmsService smsService;

    @Override
    public Page<TransactionAdminDTO> getAllTransactionsForAdmin(Pageable pageable) {
        return transactionRepository.findAllTransactions(pageable)
                .map(transactionMapper::toTransactionAdminDTO);
    }

    @Override
    public User accountSuspension(UUID uuid) {
        User user = userRepository.findById(uuid).orElseThrow(() -> new RuntimeException("User Not Found"));
        user.setEnabled(!user.isEnabled());
        return userRepository.save(user);
    }

    @Override
    public double getSystemBalance() {

        List<Wallet> systemWallets = walletRepository.findByType(WalletType.SYSTEM);
        if (systemWallets.isEmpty()) {
            return 0;
        }

        return 0.0;
    }

    @Override
    public Page<UserDTO> findFilteredUsers(String name, UserRole role, Pageable pageable) {
        Page<User> userPage;

        if (name != null && !name.isEmpty() && role != null) {
            userPage = userRepository.findByNameContainingIgnoreCaseAndRole(name, role, pageable);
        } else if (role != null) {
            userPage = userRepository.findByRole(role, pageable);
        } else if (name != null && !name.isEmpty()) {
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
        dto.setTrialEndsAt(user.getTrialEndsAt());
        dto.setSubscriptionStatus(user.getSubscriptionStatus());
        dto.setSubscriptionExpiresAt(user.getSubscriptionExpiresAt());
        dto.setFcmToken(user.getFcmToken());

        if (user.getUserProfile() != null) {
            dto.setPhone(user.getUserProfile().getPhone());
            dto.setBio(user.getUserProfile().getBio());
            dto.setDisplayName(user.getUserProfile().getDisplayName());
        }

        // Password haisafirishwi kwenye response kwa usalama wa mfumo
        return dto;
    }
    @Override
    public List<TransactionAdminDTO> getRecentTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable)
                .map(transactionMapper::toTransactionAdminDTO)
                .getContent();
    }

    @Override
    public User verifyPhotographer(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setVerified(true);
        User verifiedUser = userRepository.save(user);
        eventPublisher.publishEvent(new PhotographerVerifiedEvent(verifiedUser));

        return verifiedUser;
    }

    @Override
    public List<UserDTO> getUnverifiedPhotographers() {
        return userRepository.findByRoleAndIsVerified(UserRole.PHOTOGRAPHER, false)
                .stream()
                .map(user -> UserDTO.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        // Map other necessary fields
                        .build())
                .toList();
    }
    @Override
    public void removeAccount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user is not found !"));

        userRepository.delete(user);
        return;


    }

}