package com.jemigraph.jemigraph_backend.DTO;

import lombok.Data;

import java.util.UUID;

@Data
public class ProcessActionDTO {
  private UUID id;
  private String action;
}
