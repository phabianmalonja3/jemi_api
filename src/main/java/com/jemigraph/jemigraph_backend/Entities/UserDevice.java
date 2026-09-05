package com.jemigraph.jemigraph_backend.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_devices")
@Setter
@Getter
public class UserDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 1000)
    private String deviceToken;

    private String deviceName;

    private LocalDateTime lastActiveAt;



}