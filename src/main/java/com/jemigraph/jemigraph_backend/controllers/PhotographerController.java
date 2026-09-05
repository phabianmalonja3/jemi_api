package com.jemigraph.jemigraph_backend.controllers;

import com.jemigraph.jemigraph_backend.DTO.PackageRequestDto;
import com.jemigraph.jemigraph_backend.DTO.PkgDTO;
import com.jemigraph.jemigraph_backend.Entities.Pkg;
import com.jemigraph.jemigraph_backend.mappers.PhotographyPackageMapper;
import com.jemigraph.jemigraph_backend.mappers.PkgMapper;
import com.jemigraph.jemigraph_backend.models.LocationUpdateRequest;
import com.jemigraph.jemigraph_backend.DTO.PhotographerProfileDTO;
import com.jemigraph.jemigraph_backend.services.LiveStatusService;
import com.jemigraph.jemigraph_backend.services.PhotographerService;
import com.jemigraph.jemigraph_backend.services.UserService;
import com.jemigraph.jemigraph_backend.services.impl.PkgService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/photographers")
@AllArgsConstructor
public class PhotographerController {

    private final PhotographerService service;
    private final UserService userService;
    private final LiveStatusService liveStatusService;
    private final PhotographerService profileService;
    private final PkgService pkgService;
    private PkgMapper pkgMapper;
    private PhotographyPackageMapper photographyPackageMapper;

    @PostMapping("/online-status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @RequestBody LocationUpdateRequest request,
            Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(liveStatusService.toggleStatus(email, request));
    }
    @GetMapping
    public ResponseEntity<Page<PhotographerProfileDTO>> getAllPhotographersNear(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getPhotographers(page, size));
    }
    @PostMapping("/packages")
    public ResponseEntity<PackageRequestDto> createPackage(
            @RequestBody PkgDTO request,
            Principal principal) {
        String email = principal.getName();


        return ResponseEntity.ok(photographyPackageMapper.toDto(pkgService.createPackagePhotographer(request,email)));
    }

    @DeleteMapping("/packages/{id}")
    public ResponseEntity<?> deletePackage(@PathVariable UUID id, Principal principal ) {
        pkgService.deletePackage(id);
        return ResponseEntity.ok("Package deleted successfully.");
    }
    @GetMapping("/packages")
    public ResponseEntity<List<PackageRequestDto>> getMyPackages(
            Principal principal
    ){
        List<PackageRequestDto> packages = pkgService.getMyPackages(principal.getName())
                .stream()
                .map(pkg -> photographyPackageMapper.toDto(pkg))
                .collect(Collectors.toList());

        return ResponseEntity.ok(packages);
    }
    @GetMapping("/all")
    public ResponseEntity<Page<PhotographerProfileDTO>> getAllPhotographers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getPhotographersALll(page, size));
    }

    @PostMapping("/live/heartbeat")
    public ResponseEntity<Map<String, Object>> heartbeat(Principal principal, @RequestBody LocationUpdateRequest request) {
        Map<String, Object> response = liveStatusService.renewHeartbeat(principal.getName(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhotographerProfileDTO> getPhotographer(@PathVariable UUID id) {
        return ResponseEntity.ok(profileService.getPhotographerById(id));
    }

    @GetMapping("/nearby")
    public ResponseEntity<?> getNearby(
            @RequestParam(required = true, defaultValue = "0.0") double lat,
            @RequestParam(required = true, defaultValue = "0.0") double lng) {

        return ResponseEntity.ok(liveStatusService.getNearbyPhotographers(lat, lng));
    }


}
