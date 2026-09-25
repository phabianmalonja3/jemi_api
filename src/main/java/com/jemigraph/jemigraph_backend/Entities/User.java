package com.jemigraph.jemigraph_backend.Entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jemigraph.jemigraph_backend.enums.UserRole;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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

  @Builder.Default private Boolean isOnline = false;

  @Builder.Default private Boolean isBusy = false;

  @Builder.Default
  @Column(name = "account_non_locked", nullable = false)
  private boolean accountNonLocked = true;

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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscription_plan_id")
  private SubscriptionPlan subscriptionPlan;

  @OneToMany(mappedBy = "photographer", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Pkg> packages;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnoreProperties({"user", "hibernateLazyInitializer", "handler"})
  private List<Gallery> gallery = new ArrayList<>();

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

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return this.accountNonLocked;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }
}
