package com.jemigraph.jemigraph_backend.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "otp_verifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String email;
    private String otpCode;
    private LocalDateTime expiryTime;
}
