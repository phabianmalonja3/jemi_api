package com.jemigraph.jemigraph_backend.Entities;

import com.jemigraph.jemigraph_backend.enums.VerificationStatus;
import jakarta.persistence.*; // All persistence annotations should come from here
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@jakarta.persistence.Table(name = "photographer_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotographerProfile {

    @Id
    @Column(name = "user_id") // This ensures the DB column name is clear
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // This tells Hibernate to use the 'userId' field as the ID from the User entity
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;
    private String role;
    private String specialty;
    private String location;
    private String experience;
    private Double rating;
    private Integer sessions;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String quote;
    private String image;
    private Boolean available;

    @ElementCollection
    @CollectionTable(
            name = "photographer_gallery",
            joinColumns = @JoinColumn(name = "profile_id")
    )
    @Column(name = "image_url")
    @Builder.Default // Prevents NullPointerException when using @Builder
    private List<String> gallery = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "photographer_achievements",
            joinColumns = @JoinColumn(name = "profile_id")
    )
    @Column(name = "achievement")
    @Builder.Default
    private List<String> achievements = new ArrayList<>();

    @Embedded
    private SocialLinks social;

    @Column(name = "verification_status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Builder.Default
    private Double totalEarnings = 0.0;

    @Builder.Default
    private Integer completedBookings = 0;
}