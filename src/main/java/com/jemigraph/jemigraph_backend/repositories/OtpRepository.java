package com.jemigraph.jemigraph_backend.repositories;

import com.jemigraph.jemigraph_backend.Entities.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

import java.util.UUID;

@Repository
public interface OtpRepository extends JpaRepository<OtpVerification, UUID> {
  
    void deleteByExpiryTimeBefore(LocalDateTime now);

    OtpVerification findByEmail(String email);
}
