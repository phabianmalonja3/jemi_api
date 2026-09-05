package com.jemigraph.jemigraph_backend.events;

import lombok.Data;

@Data
public class TypingEvent {
    private String senderId;
    private boolean isTyping;
}
