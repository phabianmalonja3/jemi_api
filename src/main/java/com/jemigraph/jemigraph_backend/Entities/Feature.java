// package com.jemigraph.jemigraph_backend.Entities;
//
// import com.fasterxml.jackson.annotation.JsonIgnore;
// import com.jemigraph.jemigraph_backend.enums.FeatureCategory;
// import jakarta.persistence.*;
// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.UUID;
// import lombok.*;
// import org.hibernate.annotations.CreationTimestamp;
//
// @Entity
// @Table(
//    name = "features",
//    uniqueConstraints = {
//      @UniqueConstraint(
//          name = "uk_feature_name",
//          columnNames = {"name"})
//    })
// @Getter
// @Setter
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
// public class Feature {
//
//  @Id
//  @GeneratedValue(strategy = GenerationType.UUID)
//  @Column(columnDefinition = "UUID", updatable = false, nullable = false)
//  private UUID id;
//
//  @Column(nullable = false, unique = true, length = 100)
//  private String name; // e.g., "Drone", "4K Video", "Wedding"
//
//  @Column(length = 255)
//  private String description;
//
//  @Column(length = 100)
//  private String icon;
//
//  @Enumerated(EnumType.STRING)
//  @Column(nullable = false, length = 50)
//  private FeatureCategory category;
//
//  @Builder.Default
//  @Column(nullable = false)
//  private boolean active = true;
//
//  @Column(name = "display_order")
//  @Builder.Default
//  private Integer displayOrder = 0;
//
//  // ============================================================
//  // RELATIONSHIP: Many Features ←→ Many Users (via user_skills)
//  // ============================================================
//  @ManyToMany(mappedBy = "skills", fetch = FetchType.LAZY)
//  @JsonIgnore
//  @Builder.Default
//  private List<User> users = new ArrayList<>();
//
//  @Builder.Default
//  @CreationTimestamp
//  @Column(name = "created_at", nullable = false, updatable = false)
//  private LocalDateTime createdAt = LocalDateTime.now();
//
//  @Column(name = "updated_at")
//  private LocalDateTime updatedAt;
//
//  @PreUpdate
//  protected void onUpdate() {
//    updatedAt = LocalDateTime.now();
//  }
//
//  // ============================================================
//  // HELPER METHODS
//  // ============================================================
//  //  public void addUser(User user) {
//  //    users.add(user);
//  //    user.getSkills().add(this);
//  //  }
//  //
//  //  public void removeUser(User user) {
//  //    users.remove(user);
//  //    user.getSkills().remove(this);
//  //  }
// }
