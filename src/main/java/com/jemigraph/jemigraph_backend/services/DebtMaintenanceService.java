package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DebtMaintenanceService {
    private UserRepository userRepository;
    @Scheduled(cron = "0 0 1 * * ?") // Runs at 1 AM every day
    public void processDebtLocks() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<User> debtors = userRepository.findOverdueDebtors(sevenDaysAgo);
        for (User user : debtors) {
            user.setAccountLockedDueToDebt(true);
            userRepository.save(user);
            // Optional: Trigger push notification/email to user here
        }
    }
}
