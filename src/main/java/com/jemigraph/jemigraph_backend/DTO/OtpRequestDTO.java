package com.jemigraph.jemigraph_backend.DTO;

import lombok.Data;

@Data
public class OtpRequestDTO {
    private String otp;
    private String email;
}
