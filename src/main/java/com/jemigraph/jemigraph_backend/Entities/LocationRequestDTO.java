package com.jemigraph.jemigraph_backend.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationRequestDTO {
    private Double latitude;
    private Double longitude;
    private String address;

}
