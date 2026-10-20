package com.jemigraph.jemigraph_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhotographerProfileCompletionDto {

  private boolean hasLocation;

  private boolean hasPackages;

  private boolean profileComplete;

  private String message;
}
