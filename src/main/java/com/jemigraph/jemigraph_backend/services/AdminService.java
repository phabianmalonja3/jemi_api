package com.jemigraph.jemigraph_backend.services;
import com.jemigraph.jemigraph_backend.DTO.TransactionAdminDTO;
import com.jemigraph.jemigraph_backend.DTO.UserDTO;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface AdminService {
    User accountSuspension(UUID uuid);
     double getSystemBalance();
    Page<TransactionAdminDTO> getAllTransactionsForAdmin(Pageable pageable);
    Page<UserDTO> findFilteredUsers(String name, UserRole role, Pageable pageable);

    List<TransactionAdminDTO> getRecentTransactions(Pageable topFive);

    User verifyPhotographer(UUID userId);

    List<UserDTO> getUnverifiedPhotographers();

    void removeAccount(UUID userId);
}
