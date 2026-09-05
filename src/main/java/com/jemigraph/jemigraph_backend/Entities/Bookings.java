package com.jemigraph.jemigraph_backend.Entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jemigraph.jemigraph_backend.enums.BookingStatus;
import com.jemigraph.jemigraph_backend.enums.BookingType;
import com.jemigraph.jemigraph_backend.enums.PaymentStatus;
import com.jemigraph.jemigraph_backend.events.Review;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@Audited
public class Bookings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // 1. Photographer Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photographer_id", nullable = false)
    @JsonIgnoreProperties({"bookings", "password", "hibernateLazyInitializer", "handler"})
    private User photographer;

    // 2. Client Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonIgnoreProperties({"bookings", "password", "hibernateLazyInitializer", "handler"})
//    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private User client;

    // 3. Package Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", referencedColumnName = "id")
    private Pkg pkg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingType type;

    @Column(name = "pickup_time", nullable = true)
    private LocalDateTime pickupTime;

    @Column(name = "address_name")
    private String addressName;

    private Double lat;
    private Double lng;

    @Builder.Default
    @Column(name = "amount_paid", nullable = false, precision = 19, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"booking"})
    private Review review;
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.type == BookingType.INSTANT && this.pickupTime == null) {
            this.pickupTime = LocalDateTime.now();
        }
    }
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.type == BookingType.INSTANT && this.pickupTime == null) {
            this.pickupTime = LocalDateTime.now();
        }
    }
}