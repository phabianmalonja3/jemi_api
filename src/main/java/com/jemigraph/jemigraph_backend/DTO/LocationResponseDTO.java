package com.jemigraph.jemigraph_backend.DTO;


import lombok.Data;

import java.util.UUID;

@Data
public class LocationResponseDTO {

    private UUID uuid;
    private Double latitude;
    private Double longitude;
    private String address;
}
