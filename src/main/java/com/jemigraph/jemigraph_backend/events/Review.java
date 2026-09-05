package com.jemigraph.jemigraph_backend.events;

import com.jemigraph.jemigraph_backend.Entities.Bookings;
import com.jemigraph.jemigraph_backend.Entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor // 2. Ongeza hii
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private int rating;
    @Column(columnDefinition = "TEXT")
    private String comment;
    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Bookings booking;
    @ManyToOne
    @JoinColumn(name = "photographer_id", nullable = false)
    private User photographer;
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private User client;
}
