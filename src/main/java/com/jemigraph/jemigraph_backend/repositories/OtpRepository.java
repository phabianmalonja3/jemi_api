package com.jemigraph.jemigraph_backend.repositories;

import com.jemigraph.jemigraph_backend.Entities.OtpVerification;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpRepository extends JpaRepository<OtpVerification, UUID> {

  void deleteByExpiryTimeBefore(LocalDateTime now);

  Optional<OtpVerification> findTopByEmailOrderByIdDesc(String email);

  OtpVerification findByEmail(String email);

  void deleteByEmail(String toEmail);
}
