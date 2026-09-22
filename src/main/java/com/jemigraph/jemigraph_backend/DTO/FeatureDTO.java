package com.jemigraph.jemigraph_backend.DTO;

import com.jemigraph.jemigraph_backend.enums.FeatureCategory;
import java.util.UUID;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureDTO {
  private UUID id;
  private String name;
  private String description;
  private String icon;
  private FeatureCategory category;
}
