package com.jemigraph.jemigraph_backend.DTO;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeatureUpdateRequest {
  private List<String> featureIds;
}
