package com.jemigraph.jemigraph_backend.configs;

import com.jemigraph.jemigraph_backend.filters.JwtAuthenticationFilter;
import com.jemigraph.jemigraph_backend.utils.JwtAuthenticationEntryPoint;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

  private final UserDetailsService userDetailsService;
  private final JwtAuthenticationFilter authenticationFilter;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  private final org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) {

    return http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/ws-jemigraph/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/team/**")
                    .permitAll()
                    .requestMatchers(
                        "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**")
                    .permitAll()
                    .requestMatchers("/api-docs**")
                    .permitAll()
                    .requestMatchers("/ws-jemigrapher")
                    .permitAll()
                    .requestMatchers("/app-version/**")
                    .permitAll()
                    .requestMatchers("/auth/**")
                    .permitAll() // Login na Register
                    .requestMatchers("/subscription-plans/**")
                    .permitAll()
                    .requestMatchers("/subscription-plans/activate")
                    .hasRole("ADMIN") // Login na Register
                    .requestMatchers("/profile/**")
                    .permitAll()
                    .requestMatchers("/notifications/**")
                    .permitAll()
                    .requestMatchers("/client/**")
                    .permitAll()
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .requestMatchers("/payments/**")
                    .permitAll()
                    .requestMatchers("/user/**")
                    .authenticated()
                    .requestMatchers("/auth/**", "/client/**", "/actuator/**")
                    .permitAll()
                    // Add the versioned path here to allow public access to images
                    .requestMatchers("/api/v0.1/images/**", "/images/**", "/uploads/**")
                    .permitAll()
                    .requestMatchers("/users/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/subscriptions/**")
                    .permitAll()
                    .requestMatchers("/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/wallet/**")
                    .hasAnyRole("ADMIN", "PHOTOGRAPHER")
                    .requestMatchers(HttpMethod.GET, "/photographers/**")
                    .permitAll()
                    .requestMatchers("/photographer/**")
                    .hasRole("PHOTOGRAPHER")
                    .requestMatchers("/bookings/**")
                    .authenticated()
                    .requestMatchers("/reviews/**")
                    .permitAll()
                    .requestMatchers("/jobs/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/packages/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/packages/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/packages/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/packages/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(
            exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationProvider authProvider() {
    var provider = new DaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
    return configuration.getAuthenticationManager();
  }
}
