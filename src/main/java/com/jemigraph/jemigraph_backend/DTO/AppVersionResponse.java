package com.jemigraph.jemigraph_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppVersionResponse {

    private String appId;

    private Boolean updateRequired;

    private String latestVersion;

    private Integer latestBuildNumber;

    private String message;

    private String storeUrl;
}