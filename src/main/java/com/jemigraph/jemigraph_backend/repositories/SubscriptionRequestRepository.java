package com.jemigraph.jemigraph_backend.repositories;

import com.jemigraph.jemigraph_backend.Entities.SubscriptionRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubscriptionRequestRepository extends JpaRepository<SubscriptionRequest, UUID> {}
