package com.jemigraph.jemigraph_backend.configs;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "commission")
@Data
public class CommissionConfig {

    private BigDecimal rate = new BigDecimal("0.10");


}
