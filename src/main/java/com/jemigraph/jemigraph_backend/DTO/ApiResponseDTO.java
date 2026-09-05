// com.jemigraph.jemigraph_backend.DTO.ApiResponseDTO

package com.jemigraph.jemigraph_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDTO {
    private boolean success;
    private String message;
    private Object data;
}