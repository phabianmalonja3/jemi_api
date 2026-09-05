package com.jemigraph.jemigraph_backend.services;

import com.jemigraph.jemigraph_backend.Entities.ChatMessage;

import java.util.List;

public interface ChatService {

    ChatMessage saveMessage(ChatMessage message);
    List<ChatMessage> getChatHistory(String chatId);
}
