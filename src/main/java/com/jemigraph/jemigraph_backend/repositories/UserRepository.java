package com.jemigraph.jemigraph_backend.repositories;


import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.enums.SubscriptionStatus;
import com.jemigraph.jemigraph_backend.enums.UserRole;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {


    Optional<User> findFirstByEmail(String email);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<User> findAllByRole(UserRole role, Pageable pageable);
    Optional<User> findByIdAndRole(UUID id, UserRole role);
    List<User> findAllByRoleAndIsOnlineTrueAndIsBusyFalse(UserRole role);
    @Query("SELECT u.fcmToken FROM User u WHERE u.id = :userId")
    String findFcmTokenByUserId(@Param("userId") UUID userId);

    Optional<User> findByResetToken(String resetToken);
    List<User> findByRoleAndIsVerified(UserRole role, boolean isVerified);
//    List<PkgDTO> findAllByPackages(UserRole role, boolean isVerified);
    Page<User> findByNameContainingIgnoreCase(String name, Pageable pageable);
    @EntityGraph(attributePaths = {"userProfile"})
    Page<User> findByRole(UserRole role, Pageable pageable);
    Page<User> findByNameContainingIgnoreCaseAndRole(String name, UserRole role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.email IN :emails")
    List<User> findAllByEmailIn(@Param("emails") List<String> emails);


    List<User> findAllByIdInAndIsOnlineTrue(Collection<UUID> ids);

    List<User> findAllByIdIn(List<UUID> uuidList);

    @Query("SELECT u FROM User u WHERE u.currentDebt > 0 AND u.debtStartDate <= :threshold")
    List<User> findOverdueDebtors(@Param("threshold") LocalDateTime threshold);


    @Query("SELECT u FROM User u WHERE u.role = :role AND u.subscriptionStatus = :status AND u.subscriptionExpiresAt < :now")
    List<User> findExpiredPhotographers(
            @Param("role") UserRole role,
            @Param("status") SubscriptionStatus status,
            @Param("now") LocalDateTime now
    );


}
