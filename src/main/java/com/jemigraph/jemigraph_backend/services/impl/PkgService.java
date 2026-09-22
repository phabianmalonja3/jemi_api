package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.DTO.PhotographerPackageDTO;
import com.jemigraph.jemigraph_backend.Entities.Pkg;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.exceptions.DuplicatePackageLevelException;
import com.jemigraph.jemigraph_backend.mappers.PkgMapper;
import com.jemigraph.jemigraph_backend.repositories.PkgRepository;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PkgService {
  private final PkgRepository pkgRepository;
  private final UserRepository userRepository;
  private final PkgMapper pkgMapper;

  public List<PhotographerPackageDTO> getAllPackages() {
    return pkgRepository.findAll().stream().map(pkgMapper::toDto).toList();
  }

  public PhotographerPackageDTO createPackage(PhotographerPackageDTO phottographerPackageDTO) {
    Pkg entity = pkgMapper.toEntity(phottographerPackageDTO);
    Pkg saved = pkgRepository.save(entity);
    return pkgMapper.toDto(saved);
  }

  public PhotographerPackageDTO updatePackage(UUID id, PhotographerPackageDTO dto) {
    Pkg existingPkg =
        pkgRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Package haipo na ID: " + id));
    pkgMapper.updateEntityFromDto(dto, existingPkg);
    Pkg updatedPkg = pkgRepository.save(existingPkg);
    return pkgMapper.toDto(updatedPkg);
  }

  public void deletePackage(UUID id) {
    if (!pkgRepository.existsById(id)) {
      throw new RuntimeException("Hauwezi kufuta: Package yenye ID " + id + " haipo.");
    }
    pkgRepository.deleteById(id);
  }

  @Transactional
  public Pkg createPackagePhotographer(PhotographerPackageDTO request, String email) {

    User photographer =
        userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

    if (pkgRepository.existsByPhotographerAndName(photographer, request.getName())) {
      throw new RuntimeException("You already have a package with this name.");
    }

    boolean existsByLevel =
        pkgRepository.existsByPhotographerAndLevel(photographer, request.getLevel());
    if (existsByLevel) {
      throw new DuplicatePackageLevelException(
          String.format(
              "You already have a %s package. Only one package per level is allowed.",
              request.getLevel().toString()));
    }
    Pkg pkg = new Pkg();
    pkg.setName(request.getName());
    pkg.setDuration(request.getDuration());
    pkg.setPrice(request.getPrice());
    pkg.setLevel(request.getLevel());
    pkg.setFeatures(request.getFeatures());
    pkg.setPhotographer(photographer);

    return pkgRepository.save(pkg);
  }

  public List<Pkg> getMyPackages(String email) {
    User photographer =
        userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    return pkgRepository.findByPhotographer(photographer);
  }
}
