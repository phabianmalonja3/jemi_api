package com.jemigraph.jemigraph_backend.services;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.function.Function;

@Service
public interface JwtService {
    String generateAccessToken(String email);
    String generateRefreshToken(String email);
    boolean isTokenValid(String token, UserDetails userDetails);
    String extractUsername(String token);
   <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    boolean isTokenExpired(String token);
    Date extractExpiration(String token);

}
