package com.gymapp.backend.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/public")
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "Backend is working!";
    }

    @GetMapping("/secure-test")
    public ResponseEntity<String> secureTest(@RequestHeader("Authorization") String idToken) {
        try {
            FirebaseAuth.getInstance().verifyIdToken(idToken.replace("Bearer ", ""));
            return ResponseEntity.ok("Secure endpoint is working! User is authenticated.");
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(401).body("Unauthorized: " + e.getMessage());
        }
    }
    
    @PostMapping("/debug")
    public Map<String, Object> debugRequest(HttpServletRequest request, @RequestBody(required = false) Map<String, Object> payload) {
        log.info("Debug endpoint called");
        Map<String, Object> response = new HashMap<>();
        
        // Request details
        response.put("method", request.getMethod());
        response.put("requestURI", request.getRequestURI());
        response.put("queryString", request.getQueryString());
        response.put("contentType", request.getContentType());
        
        // Headers
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        response.put("headers", headers);
        
        // Payload
        response.put("payload", payload);
        
        log.info("Debug response: {}", response);
        return response;
    }
    
    @PostMapping("/testsync")
    public Map<String, Object> testSync(HttpServletRequest request, @RequestBody Map<String, Object> payload) {
        log.info("Test sync endpoint called with method: {} and payload: {}", request.getMethod(), payload);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "POST request successful");
        response.put("receivedPayload", payload);
        
        return response;
    }
} 