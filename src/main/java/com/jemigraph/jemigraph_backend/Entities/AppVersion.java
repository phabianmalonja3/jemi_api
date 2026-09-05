package com.jemigraph.jemigraph_backend.Entities;

import com.jemigraph.jemigraph_backend.enums.AppPlatform;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;
@Entity
@Table(name = "app_versions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "app_id", nullable = false, length = 150)
    private String appId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppPlatform platform;

    @Column(nullable = false, length = 20)
    private String currentVersion;

    @Column(nullable = false)
    private Integer currentBuildNumber;

    @Column(nullable = false)
    private Integer minimumBuildNumber;

    @Column(columnDefinition = "TEXT")
    private String updateMessage;

    @Column(length = 500)
    private String storeUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}