package com.gymapp.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Allow all origins for testing - you can restrict this later
        config.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // Configure allowed methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Configure allowed headers
        config.setAllowedHeaders(Arrays.asList(
            "Origin", 
            "Content-Type",
            "Accept", 
            "Authorization", 
            "X-Requested-With", 
            "Access-Control-Request-Method", 
            "Access-Control-Request-Headers"
        ));
        
        // Allow credentials
        config.setAllowCredentials(true);
        
        // Expose headers
        config.setExposedHeaders(Arrays.asList("Authorization"));
        
        // Max age
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
} 