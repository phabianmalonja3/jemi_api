package com.jemigraph.jemigraph_backend.repositories;

import com.jemigraph.jemigraph_backend.Entities.Pkg;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.enums.PackageLevel;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PkgRepository extends JpaRepository<Pkg, UUID> {

  List<Pkg> findByPhotographer(User photographer);

  List<Pkg> findByPhotographer_Id(UUID photographerId);

  boolean existsByPhotographerAndLevel(User photographer, PackageLevel level);

  boolean existsByName(String name);

  boolean existsByPhotographerAndName(User photographer, String name);

  Optional<Pkg> findByName(String trial);
}
