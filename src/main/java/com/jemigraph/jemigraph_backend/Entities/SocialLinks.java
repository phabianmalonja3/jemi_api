package com.jemigraph.jemigraph_backend.Entities;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Embeddable
public class SocialLinks {

    private String instagram;
    private String twitter;
    private String email;
}