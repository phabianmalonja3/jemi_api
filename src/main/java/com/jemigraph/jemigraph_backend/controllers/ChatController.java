package com.jemigraph.jemigraph_backend.controllers;

import com.jemigraph.jemigraph_backend.Entities.ChatMessage;
import com.jemigraph.jemigraph_backend.events.TypingEvent;
import com.jemigraph.jemigraph_backend.repositories.ChatRepository;
import com.jemigraph.jemigraph_backend.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final ChatRepository chatRepository;

    @MessageMapping("/chat.sendMessage/{bookingId}")
    @SendTo("/topic/chat/{bookingId}")
    public ChatMessage sendMessage(@DestinationVariable String bookingId, @Payload ChatMessage chatMessage) {
        chatMessage.setChatId(bookingId);
        return chatRepository.save(chatMessage);
    }
    @MessageMapping("/chat.typing/{chatId}")
    @SendTo("/topic/chat/{chatId}/typing")
    public TypingEvent handleTyping(@DestinationVariable String chatId, TypingEvent typingEvent){
        return typingEvent;
    }
}