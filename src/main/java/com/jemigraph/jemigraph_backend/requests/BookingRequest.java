package com.jemigraph.jemigraph_backend.requests;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    private String id;
    private String clientName;
    private String email;
    private double lat;
    private double lng;
    private String serviceType="STREET_PHOTO";
}