package com.jemigraph.jemigraph_backend.models;

import lombok.Data;

@Data
public class LocationUpdateRequest {
    private boolean online;
    private Double latitude;
    private Double longitude;
}