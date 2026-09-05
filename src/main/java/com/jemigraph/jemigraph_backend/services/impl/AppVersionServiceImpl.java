package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.DTO.AppVersionRequest;
import com.jemigraph.jemigraph_backend.DTO.AppVersionResponse;
import com.jemigraph.jemigraph_backend.Entities.AppVersion;
import com.jemigraph.jemigraph_backend.enums.AppPlatform;
import com.jemigraph.jemigraph_backend.repositories.AppVersionRepository;
import com.jemigraph.jemigraph_backend.services.AppVersionService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppVersionServiceImpl implements AppVersionService {

    private final AppVersionRepository appVersionRepository;

    @Override
    public AppVersionResponse checkUpdate(
            String appId,
            AppPlatform platform,
            Integer buildNumber
    ) {

        AppVersion appVersion =
                appVersionRepository
                        .findByAppIdAndPlatformAndActiveTrue(
                                appId,
                                platform
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "App version configuration not found for "
                                                + appId
                                                + " / "
                                                + platform
                                )
                        );

        boolean updateRequired =
                buildNumber < appVersion.getMinimumBuildNumber();

        return AppVersionResponse.builder()
                .updateRequired(updateRequired)
                .latestVersion(
                        appVersion.getCurrentVersion()
                )
                .latestBuildNumber(
                        appVersion.getCurrentBuildNumber()
                )
                .message(
                        updateRequired
                                ? appVersion.getUpdateMessage()
                                : "Application is up to date"
                )
                .storeUrl(
                        appVersion.getStoreUrl()
                )
                .build();
    }

    @Override
    @Transactional
    public AppVersion create(AppVersionRequest request) {

        // Disable currently active version
        appVersionRepository
                .findByAppIdAndPlatformAndActiveTrue(
                        request.getAppId(),
                        request.getPlatform()
                )
                .ifPresent(existing -> {
                    existing.setActive(false);
                    appVersionRepository.save(existing);
                });

        // Create new active version
        AppVersion appVersion = AppVersion.builder()
                .appId(request.getAppId())
                .platform(request.getPlatform())
                .currentVersion(request.getCurrentVersion())
                .currentBuildNumber(
                        request.getCurrentBuildNumber()
                )
                .minimumBuildNumber(
                        request.getMinimumBuildNumber()
                )
                .updateMessage(
                        request.getUpdateMessage()
                )
                .storeUrl(
                        request.getStoreUrl()
                )
                .active(true)
                .build();

        return appVersionRepository.save(appVersion);
    }

    @Override
    public List<AppVersion> getAllVersions() {
        return appVersionRepository.findAll();
    }
}