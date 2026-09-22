package com.jemigraph.jemigraph_backend.Entities;

import com.jemigraph.jemigraph_backend.enums.PackageLevel;
import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Entity
@Table(
        name = "pkg",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_photographer_package_name",
                        columnNames = {"photographer_id", "name"}
                ),
                @UniqueConstraint(
                        name = "uk_photographer_package_level",
                        columnNames = {"photographer_id", "package_level"}
                )
        }
)
@Data
public class Pkg {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer duration; // Changed from String to Integer (represents hours)

    @Column(nullable = false)
    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, name = "package_level")
    private PackageLevel level;

    @ManyToOne
    @JoinColumn(name = "photographer_id", nullable = true)
    private User photographer;

    @ElementCollection
    @CollectionTable(name = "package_package_features", joinColumns = @JoinColumn(name = "package_id"))
    @Column(name = "feature")
    private List<String> features;
}