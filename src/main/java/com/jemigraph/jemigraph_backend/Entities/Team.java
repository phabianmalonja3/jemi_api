package com.jemigraph.jemigraph_backend.Entities;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

// Example Spring Boot Entity Blueprint
@Entity
@Table(name = "team_members")
@Data
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String imageUrl;

    private String instagramUrl;
    private String twitterUrl;

    private Integer displayOrder;
}