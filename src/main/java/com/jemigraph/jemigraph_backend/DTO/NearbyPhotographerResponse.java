package com.jemigraph.jemigraph_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NearbyPhotographerResponse {
    private String email;
    private String name; // Unaweza kuvuta jina kutoka DB baadaye
    private String distance; // Mfano: "1.2 km"
    private String expectedTime; // Mfano: "5 mins"
    private Double latitude;
    private Double longitude;
}