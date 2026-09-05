package com.jemigraph.jemigraph_backend.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class ProfileUpdateDTO {

    @NotBlank(message = "Full name cannot be blank")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    private String name;

    @Size(max = 30, message = "Display name cannot exceed 30 characters")
    private String displayName;

    // Validates international formats, local 07... / 06... formats, and lengths between 9 to 13 digits
//    @Pattern(regexp = "^(\\+?\\d{1,3})?[-.\\s]?\\d{9,13}$", message = "Invalid phone number format")
    private String phone;

    @Size(max = 100, message = "Location cannot exceed 100 characters")
    private String location;

    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;

    @URL(message = "Instagram link must be a valid URL")
    private String instagram;

    @URL(message = "Facebook link must be a valid URL")
    private String facebook;

    @URL(message = "Twitter link must be a valid URL")
    private String twitter;

    @URL(message = "LinkedIn link must be a valid URL")
    private String linkedin;

    @URL(message = "Website link must be a valid URL")
    private String website;
}