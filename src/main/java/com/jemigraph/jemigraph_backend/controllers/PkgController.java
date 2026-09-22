package com.jemigraph.jemigraph_backend.controllers;

import com.jemigraph.jemigraph_backend.DTO.PhotographerPackageDTO;
import com.jemigraph.jemigraph_backend.services.impl.PkgService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/packages")
@RequiredArgsConstructor
public class PkgController {
  private final PkgService pkgService;

  @GetMapping
  public List<PhotographerPackageDTO> getAllPackages() {
    return pkgService.getAllPackages();
  }

  @PostMapping
  public ResponseEntity<PhotographerPackageDTO> createPackage(
      @RequestBody PhotographerPackageDTO dto) {

    return ResponseEntity.ok(pkgService.createPackage(dto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePackage(@PathVariable UUID id) {
    pkgService.deletePackage(id);

    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<PhotographerPackageDTO> updatePackage(
      @PathVariable UUID id, @Valid @RequestBody PhotographerPackageDTO dto) {

    PhotographerPackageDTO updated = pkgService.updatePackage(id, dto);
    return ResponseEntity.ok(updated);
  }
}
