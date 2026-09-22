package com.jemigraph.jemigraph_backend.DTO;

import java.util.List;
import java.util.UUID;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSkillsRequest {
  private List<UUID> featureIds;
}
