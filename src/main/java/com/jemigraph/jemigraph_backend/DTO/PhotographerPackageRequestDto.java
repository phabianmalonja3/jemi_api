package com.jemigraph.jemigraph_backend.DTO;

import com.jemigraph.jemigraph_backend.enums.PackageLevel;
import java.util.List;
import lombok.Data;

@Data
public class PhotographerPackageRequestDto {
  private String id;
  private String name;
  private int duration;
  private Double price;
  private PackageLevel level;
  private List<String> features;
}
