package com.jemigraph.jemigraph_backend.controllers;

import com.jemigraph.jemigraph_backend.Entities.ChatMessage;
import com.jemigraph.jemigraph_backend.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatHistoryController {
    private final ChatService chatService;

    @GetMapping("/{chatId}/history")
    public ResponseEntity<List<ChatMessage>> getChatMessage(@PathVariable String chatId) {
        List<ChatMessage> history = chatService.getChatHistory(chatId);
        return ResponseEntity.ok(Objects.requireNonNullElse(history, Collections.emptyList()));
    }


}
