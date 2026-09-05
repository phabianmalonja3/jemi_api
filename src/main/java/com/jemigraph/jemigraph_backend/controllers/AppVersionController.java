package com.jemigraph.jemigraph_backend.controllers;

import com.jemigraph.jemigraph_backend.DTO.AppVersionRequest;
import com.jemigraph.jemigraph_backend.DTO.AppVersionResponse;
import com.jemigraph.jemigraph_backend.Entities.AppVersion;
import com.jemigraph.jemigraph_backend.enums.AppPlatform;
import com.jemigraph.jemigraph_backend.services.AppVersionService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app-version")
@RequiredArgsConstructor
public class AppVersionController {

    private final AppVersionService appVersionService;

    @GetMapping
    public ResponseEntity<AppVersionResponse> checkUpdate(
            @RequestParam String appId,
            @RequestParam AppPlatform platform,
            @RequestParam Integer buildNumber
    ) {
        AppVersionResponse response =
                appVersionService.checkUpdate(
                        appId,
                        platform,
                        buildNumber
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<AppVersion> create(

            @RequestBody AppVersionRequest request

    ) {

        AppVersion saved =
                appVersionService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @GetMapping("/all")
    public ResponseEntity<List<AppVersion>> getAllVersions() {

        List<AppVersion> versions =
                appVersionService.getAllVersions();

        return ResponseEntity.ok(versions);
    }
}