package com.jemigraph.jemigraph_backend.repositories;

import com.jemigraph.jemigraph_backend.Entities.PhotographerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PhotographerProfileRepository extends JpaRepository<PhotographerProfile, UUID> {
    Optional<PhotographerProfile> findByUserId(UUID userId);
}
