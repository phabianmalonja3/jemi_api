package com.jemigraph.jemigraph_backend.Entities;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator; // Muhimu kwa Hibernate 6+
import java.util.UUID;

@Entity
@Table(name = "user_locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private Double latitude;
    private Double longitude;
    private String address;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
