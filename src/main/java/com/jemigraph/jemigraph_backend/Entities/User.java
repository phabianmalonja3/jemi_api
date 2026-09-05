package com.jemigraph.jemigraph_backend.Entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jemigraph.jemigraph_backend.enums.AccessStatus;
import com.jemigraph.jemigraph_backend.enums.SubscriptionStatus;
import com.jemigraph.jemigraph_backend.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    private String name;

    private BigDecimal currentDebt = BigDecimal.ZERO;
    private LocalDateTime debtStartDate;
    @Column(name = "account_locked_due_to_debt", nullable = false, columnDefinition = "boolean default false")
    private boolean accountLockedDueToDebt = false;
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    private boolean is2faEnabled = false;

    @Column(length = 64)
    private String tfaSecret;

    private String resetToken;
    private LocalDateTime resetTokenExpiry;
    @Builder.Default
    @Column(name = "enabled")
    private boolean enabled = true;

    @Builder.Default
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "fcm_token")
    private String fcmToken;

    private Double averageRating = 0.0;
    private Long totalReviews = 0L;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Builder.Default
    private Boolean isOnline = false;

    @Builder.Default
    private Boolean isBusy = false;

    @Column(name = "is_verified")
    private boolean isVerified;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Location location;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "bigint default 1")
    private Long tokenVersion = 0L;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnoreProperties("user")
    private UserProfile userProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status")
    @Builder.Default
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.INACTIVE;

    @Column(name = "subscription_expires_at")
    private LocalDateTime subscriptionExpiresAt;

    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_plan_id")
    private SubscriptionPlan subscriptionPlan;

    public void setUserProfile(UserProfile userProfile) {
        if (userProfile == null) {
            if (this.userProfile != null) {
                this.userProfile.setUser(null);
            }
        } else {
            userProfile.setUser(this);
        }
        this.userProfile = userProfile;
    }

    public void updateDebtStatus() {
        if (currentDebt.compareTo(BigDecimal.ZERO) > 0 && debtStartDate != null) {
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(debtStartDate, LocalDateTime.now());
            if (daysOverdue > 7) {
                this.accountLockedDueToDebt = true;
            }
        }
    }


    public boolean isOnTrial() {
        return this.trialEndsAt != null && LocalDateTime.now().isBefore(this.trialEndsAt);
    }

    public AccessStatus getAccessStatus(){

        LocalDateTime now = LocalDateTime.now();


        // Active subscription
        if(subscriptionStatus == SubscriptionStatus.ACTIVE
                && subscriptionExpiresAt != null
                && now.isBefore(subscriptionExpiresAt)){

            return AccessStatus.ACTIVE;
        }


        // 30 days trial
        if(trialEndsAt != null
                && now.isBefore(trialEndsAt)){

            return AccessStatus.TRIAL;
        }


        // Grace period after subscription expiry
        if(subscriptionExpiresAt != null
                && now.isBefore(subscriptionExpiresAt.plusDays(3))){

            return AccessStatus.GRACE_PERIOD;
        }


        return AccessStatus.EXPIRED;
    }


    public boolean hasActiveAccess(){

        AccessStatus status = getAccessStatus();

        return status == AccessStatus.ACTIVE
                || status == AccessStatus.TRIAL
                || status == AccessStatus.GRACE_PERIOD;
    }
    @OneToMany(mappedBy = "photographer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pkg> packages;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"user", "hibernateLazyInitializer", "handler"})
    private List<Gallery> gallery = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLockedDueToDebt;
    }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return enabled; }
}