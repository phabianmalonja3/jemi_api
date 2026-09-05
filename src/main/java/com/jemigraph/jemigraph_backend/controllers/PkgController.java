package com.jemigraph.jemigraph_backend.controllers;


import com.jemigraph.jemigraph_backend.DTO.PkgDTO;
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
    public List<PkgDTO> getAllPackages() {
        return pkgService.getAllPackages();
    }

    @PostMapping
    public ResponseEntity<PkgDTO> createPackage(@RequestBody PkgDTO dto) {

        return ResponseEntity.ok(pkgService.createPackage(dto));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePackage(@PathVariable UUID id) {
        pkgService.deletePackage(id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<PkgDTO> updatePackage(
            @PathVariable UUID id,
            @Valid @RequestBody PkgDTO dto) {

        PkgDTO updated = pkgService.updatePackage(id, dto);
        return ResponseEntity.ok(updated);
    }
}
