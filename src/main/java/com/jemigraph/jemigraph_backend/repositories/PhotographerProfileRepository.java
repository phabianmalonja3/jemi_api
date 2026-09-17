package com.jemigraph.jemigraph_backend.repositories;

import com.jemigraph.jemigraph_backend.Entities.PhotographerProfile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotographerProfileRepository extends JpaRepository<PhotographerProfile, UUID> {
  Optional<PhotographerProfile> findByUserId(UUID userId);

  Page<PhotographerProfile> findAllByOrderByRatingDesc(Pageable pageable);
}
