package com.jemigraph.jemigraph_backend.DTO;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ApiErrorResponseDTO {
  private int status;
  private String message;
  private LocalDateTime timestamp;
  private String path;

  public ApiErrorResponseDTO(int value, String s, Object o) {}
}
