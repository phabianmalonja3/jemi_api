package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.DTO.AppVersionRequest;
import com.jemigraph.jemigraph_backend.DTO.AppVersionResponse;
import com.jemigraph.jemigraph_backend.Entities.AppVersion;
import com.jemigraph.jemigraph_backend.enums.AppPlatform;

import java.util.List;

public interface AppVersionService {

    AppVersionResponse checkUpdate(
            String appId,
            AppPlatform platform,
            Integer buildNumber
    );

    AppVersion create(
            AppVersionRequest appVersion
    );

    List<AppVersion> getAllVersions();
}