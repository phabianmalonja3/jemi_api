package com.jemigraph.jemigraph_backend.DTO;

import lombok.Data;

@Data
public class JobRequest {

    private String bookingId;
    private String clientName;
    private String clientPhone;

    private String email;
    private String packageType;
    private Double price;
    private Double lat;
    private Double lng;
    private String specialRequests;
}
