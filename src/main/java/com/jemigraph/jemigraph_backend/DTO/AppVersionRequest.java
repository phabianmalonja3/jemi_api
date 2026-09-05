package com.jemigraph.jemigraph_backend.DTO;

import com.jemigraph.jemigraph_backend.enums.AppPlatform;
import lombok.Data;

@Data
public class AppVersionRequest {

    private String appId; // Added to distinguish between your two apps

    private AppPlatform platform;

    private String currentVersion;

    private Integer currentBuildNumber;

    private Integer minimumBuildNumber;

    private String updateMessage;

    private String storeUrl;

    private Boolean active;

}