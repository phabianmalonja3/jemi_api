package com.jemigraph.jemigraph_backend.DTO;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiErrorResponseDTO {
  private int status;
  private String message;
  private LocalDateTime timestamp;
  private String path;
}
