package com.jemigraph.jemigraph_backend.filters;

 // Make sure this matches your User entity import
import com.jemigraph.jemigraph_backend.Entities.User;
import com.jemigraph.jemigraph_backend.repositories.UserRepository; // Import your repository
import com.jemigraph.jemigraph_backend.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository; // Inject UserRepository to check token version

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String jwt = null;
        final String userEmail;

        // 1. JARIBU KUPATA TOKEN KUTOKA KWENYE COOKIE (Priority)
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        // 2. KAMA COOKIE HAKUNA, JARIBU KUSOMA AUTHORIZATION HEADER (Fallback)
        if (jwt == null) {
            final String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
            }
        }

        // KAMA HAKUNA TOKEN KOTE (Cookie wala Header), ENDELEA NA FILTER NYINGINE
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. TOA EMAIL KUTOKA KWENYE JWT
        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            // Token ikileta hitilafu yoyote, usimpe access
            filterChain.doFilter(request, response);
            return;
        }

        // 4. KAMA USEREMAIL IKO SAWA NA HAJAINGIA KWENYE SECURITY CONTEXT
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            try {

                Long tokenVersionFromJwt = jwtService.extractClaim(jwt, claims -> claims.get("tokenVersion", Long.class));


                User dbUser = userRepository.findByEmail(userEmail).orElse(null);


                if (dbUser != null &&
                        dbUser.getTokenVersion() != null &&
                        dbUser.getTokenVersion().equals(tokenVersionFromJwt) &&
                        jwtService.isTokenValid(jwt, userDetails)) {

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));


                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {

                filterChain.doFilter(request, response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}