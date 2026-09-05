package com.jemigraph.jemigraph_backend.events;

import com.jemigraph.jemigraph_backend.Entities.User;

import java.util.UUID;

public record PaymentInitiationEvent(User user, UUID planId, String phoneNumber) {}
