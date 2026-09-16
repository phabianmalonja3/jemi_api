package com.jemigraph.jemigraph_backend.configs;

import com.jemigraph.jemigraph_backend.services.UserRatingSeedService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeederConfig {
  @Bean
  CommandLineRunner runSeeders(UserRatingSeedService ratingSeeder) {
    return args -> {
      ratingSeeder.seedDefaultUserRatings();
    };
  }
}
