
package com.jemigraph.jemigraph_backend.Entities;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jemigraph.jemigraph_backend.enums.FileUploadType;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity

@Data
@Table(name = "galleries")
public class Gallery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileUploadType fileType;

    @Column(nullable = false)
    private String fileUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"galleries", "hibernateLazyInitializer", "handler"}) // Ongeza hii
    private User user;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}