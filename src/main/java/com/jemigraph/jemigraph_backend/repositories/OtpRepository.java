package com.jemigraph.jemigraph_backend.repositories;

import com.jemigraph.jemigraph_backend.Entities.OtpVerification;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface OtpRepository extends JpaRepository<OtpVerification, UUID> {

  @Modifying
  @Transactional
  void deleteByExpiryTimeBefore(LocalDateTime now);

  Optional<OtpVerification> findTopByEmailOrderByIdDesc(String email);

  OtpVerification findByEmail(String email);

  @Modifying
  @Transactional
  void deleteByEmail(String toEmail);
}
