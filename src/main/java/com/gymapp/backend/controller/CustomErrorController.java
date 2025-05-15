package com.gymapp.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");
        String path = (String) request.getAttribute("javax.servlet.error.request_uri");
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now().toString());
        errorDetails.put("status", statusCode != null ? statusCode : HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorDetails.put("error", exception != null ? exception.getMessage() : "Unknown error");
        errorDetails.put("path", path);
        
        log.error("Error occurred on path: {}, status: {}, message: {}", 
                  path, statusCode, exception != null ? exception.getMessage() : "Unknown error");
        
        if (exception != null) {
            log.error("Exception details:", exception);
        }
        
        return new ResponseEntity<>(errorDetails, 
                                   statusCode != null ? HttpStatus.valueOf(statusCode) : HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @RequestMapping("/public/health")
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now().toString());
        health.put("database", "Connected");
        
        return health;
    }
} 