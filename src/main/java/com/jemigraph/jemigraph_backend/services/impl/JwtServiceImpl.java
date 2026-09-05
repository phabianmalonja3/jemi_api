package com.jemigraph.jemigraph_backend.services.impl;

import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.repositories.UserRepository;
import com.jemigraph.jemigraph_backend.services.JwtService;
import com.jemigraph.jemigraph_backend.configs.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtConfig jwtConfig;
    private final UserRepository userRepository;
    private SecretKey signInKey;
    long threeMonthsInMillis = 7776000000L;

    @PostConstruct
    protected void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtConfig.getSecret());
        this.signInKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateAccessToken(String email) {
        long fifteenMinutesInMillis = 900000L;
        return buildToken(new HashMap<>(), email, fifteenMinutesInMillis);
    }

    @Override
    public String generateRefreshToken(String email) {
        return buildToken(new HashMap<>(), email, threeMonthsInMillis);
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    @Override
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    @Override
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signInKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String buildToken(Map<String, Object> extraClaims, String email, long expiration) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));

        // Inject tokenVersion and role into JWT claims
        extraClaims.put("tokenVersion", user.getTokenVersion());
        extraClaims.put("role", user.getRole()); // Assumes user.getRole() returns a String or Enum

        return Jwts.builder()
                .claims(extraClaims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(signInKey)
                .compact();
    }
}