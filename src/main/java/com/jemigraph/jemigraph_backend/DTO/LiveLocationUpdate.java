package com.jemigraph.jemigraph_backend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class LiveLocationUpdate {
    @JsonProperty("userId")
    private String userId;
    @JsonProperty("bookingId")
    private String bookingId;
    @JsonProperty("latitude")
    private double latitude;
    @JsonProperty("longitude")
    private double longitude;
}
