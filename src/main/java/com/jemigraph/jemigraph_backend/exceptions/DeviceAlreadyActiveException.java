package com.jemigraph.jemigraph_backend.exceptions;

public class DeviceAlreadyActiveException extends RuntimeException {
    public DeviceAlreadyActiveException(String message) {
        super(message);
    }
}