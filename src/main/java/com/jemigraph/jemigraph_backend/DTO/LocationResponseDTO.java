package com.jemigraph.jemigraph_backend.DTO;


import java.util.UUID;
import lombok.Data;

@Data
public class LocationResponseDTO {
    private UUID uuid;
    private Double latitude;
    private Double longitude;
    private String address;
}
