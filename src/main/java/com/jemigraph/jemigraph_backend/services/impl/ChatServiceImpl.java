package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.Entities.ChatMessage;
import com.jemigraph.jemigraph_backend.repositories.ChatRepository;
import com.jemigraph.jemigraph_backend.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@RequiredArgsConstructor
@Service
public class ChatServiceImpl implements ChatService {

    private  final ChatRepository chatRepository;
    @Override
    public ChatMessage saveMessage(ChatMessage message) {
        if(message.getTimestamp() == null){

            message.setTimestamp(LocalDateTime.now());

        }
        return chatRepository.save(message) ;
    }

    @Override
    public List<ChatMessage> getChatHistory(String chatId) {
        return chatRepository.findByChatIdOrderByTimestampAsc(chatId);
    }
}
