package com.jemigraph.jemigraph_backend.requests;

import lombok.NonNull;

public record AuthenticationRequest(
        @NonNull

        String email, @NonNull String password,String fcmToken) {
}
