package com.jemigraph.jemigraph_backend.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_id", columnList = "chat_id")
})
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Hapa utapitisha bookingId kama chatId
    @Column(name = "chat_id", nullable = false)
    private String chatId;

    @Column(name = "sender_id", nullable = false)
    private String senderId;
    
    @Column(name = "recipient_id")
    private String recipientId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}