package com.gymapp.backend.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.firebase.auth.FirebaseAuthException;

public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthenticationFilter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip authentication for these paths
        return path.startsWith("/api/") || 
               path.startsWith("/public/") ||
               !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Skip authentication for /api/users/sync
        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            handleAuthenticationError(response, "No valid authorization token provided");
            return;
        }

        String idToken = authHeader.substring(7);
        logger.info("Received token: {}", idToken.substring(0, 20) + "..."); // Log first 20 chars of token
        
        try {
            logger.info("Attempting to verify token with Firebase...");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken, true); // Force check revocation
            
            // Log token details for debugging
            logger.info("Token successfully verified");
            logger.info("Token UID: {}", decodedToken.getUid());
            logger.info("Token Email: {}", decodedToken.getEmail());
            logger.info("Token Issuer: {}", decodedToken.getIssuer());
            
            String uid = decodedToken.getUid();
            
            // Get user roles from custom claims or default to USER
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            Map<String, Object> claims = decodedToken.getClaims();
            
            if (claims != null && claims.containsKey("role")) {
                String role = claims.get("role").toString();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
            } else {
                // Default role if no custom claims
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }
            
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                uid,
                null,
                authorities
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("Authentication successful for user: {} with roles: {}", uid, authorities);
            filterChain.doFilter(request, response);
        } catch (FirebaseAuthException e) {
            logger.error("Firebase authentication error: {}", e.getMessage());
            String errorMessage = "Authentication failed: ";
            if (e.getMessage().contains("expired")) {
                errorMessage += "Token has expired. Please refresh your token.";
            } else {
                errorMessage += e.getMessage();
            }
            handleAuthenticationError(response, errorMessage);
        } catch (Exception e) {
            logger.error("Unexpected error during authentication: {}", e.getMessage());
            handleAuthenticationError(response, "Authentication failed: " + e.getMessage());
        }
    }

    private void handleAuthenticationError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", Instant.now().toString());
        error.put("status", "UNAUTHORIZED");
        
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
} 