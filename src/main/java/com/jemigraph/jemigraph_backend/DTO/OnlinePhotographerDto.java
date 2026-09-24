package com.jemigraph.jemigraph_backend.DTO;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OnlinePhotographerDto {
  private UUID id;
  private String name;
  private String email;
  private String phone;
  private double latitude;
  private double longitude;
}
