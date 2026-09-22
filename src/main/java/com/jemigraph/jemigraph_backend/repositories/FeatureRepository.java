// package com.jemigraph.jemigraph_backend.repositories;
//
//
// import com.jemigraph.jemigraph_backend.enums.FeatureCategory;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository;
//
// import java.util.List;
// import java.util.Optional;
// import java.util.UUID;
//
// @Repository
// public interface FeatureRepository extends JpaRepository<Feature, UUID> {
//
//  Optional<Feature> findByNameIgnoreCase(String name);
//
//  List<Feature> findByCategoryAndActiveTrueOrderByDisplayOrderAsc(FeatureCategory category);
//
//  List<Feature> findByActiveTrueOrderByCategoryAscDisplayOrderAsc();
//
//  List<Feature> findByNameIn(List<String> names);
// }
