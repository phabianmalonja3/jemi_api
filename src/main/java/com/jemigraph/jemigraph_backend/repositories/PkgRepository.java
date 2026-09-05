package com.jemigraph.jemigraph_backend.repositories;
import com.jemigraph.jemigraph_backend.DTO.PkgDTO;
import com.jemigraph.jemigraph_backend.Entities.Pkg;
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.enums.PackageLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PkgRepository extends JpaRepository<Pkg, UUID> {
    
    List<Pkg> findByPhotographer(User photographer);

    List<Pkg> findByPhotographer_Id(UUID photographerId);
    boolean existsByPhotographerAndLevel(User photographer, PackageLevel level);

    boolean existsByName(String name);

    boolean existsByPhotographerAndName(User photographer, String name);
}
