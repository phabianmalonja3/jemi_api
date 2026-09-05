package com.jemigraph.jemigraph_backend.Entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Data // Inajumuisha @Getter, @RequiredArgsConstructor, @ToString, @EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    @Setter(AccessLevel.NONE)
    private UUID id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;
    @Column(name = "display_name")
    private String displayName;

    @Column(length = 20)
    private String phone;

    private String location;
    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "profile_image")
    private String profileImage;
    private String instagram;
    private String facebook;
    private String twitter;
    private String linkedin;
    private String website;
    @Column(name = "created_at", updatable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Setter(AccessLevel.NONE)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}