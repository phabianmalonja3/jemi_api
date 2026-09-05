package com.jemigraph.jemigraph_backend.repositories;
import com.jemigraph.jemigraph_backend.Entities.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {
    Optional<Location> findByUserId(UUID userId);
}
