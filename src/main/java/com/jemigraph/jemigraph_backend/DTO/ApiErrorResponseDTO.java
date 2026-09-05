package com.jemigraph.jemigraph_backend.DTO;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApiErrorResponseDTO {
    private int status;
    private String message;
    private LocalDateTime timestamp;
    private String path;

    public ApiErrorResponseDTO(int status, String message, String path) {
        this.status = status;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}