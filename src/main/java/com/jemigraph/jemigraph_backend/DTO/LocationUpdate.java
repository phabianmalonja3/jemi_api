package com.jemigraph.jemigraph_backend.DTO;

import lombok.Data;

@Data
public class LocationUpdate {
    private String bookingId;
    private String photographerId;
    private double lat;
    private double lng;
}