package com.jemigraph.jemigraph_backend.repositories;

import com.jemigraph.jemigraph_backend.Entities.AppVersion;
import com.jemigraph.jemigraph_backend.enums.AppPlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppVersionRepository
        extends JpaRepository<AppVersion, UUID> {

    Optional<AppVersion> findByPlatformAndActiveTrue(
            AppPlatform platform
    );

    Optional<AppVersion> findByAppIdAndPlatformAndActiveTrue(
            String appId,
            AppPlatform platform
    );
}