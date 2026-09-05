package com.jemigraph.jemigraph_backend.Entities;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;


    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "booking_id")
    private UUID bookingId;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    // Muda ambao arifa imetengenezwa
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


}