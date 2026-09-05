package com.jemigraph.jemigraph_backend.repositories;

import com.jemigraph.jemigraph_backend.Entities.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findByChatIdOrderByTimestampAsc(String chatid);
}
