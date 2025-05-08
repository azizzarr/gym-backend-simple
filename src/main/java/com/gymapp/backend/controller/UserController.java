package com.gymapp.backend.controller;

import com.gymapp.backend.model.User;
import com.gymapp.backend.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class UserController {
    private final UserService userService;

    @PostMapping("/sync")
    public ResponseEntity<User> syncUser(@RequestBody Map<String, String> payload) {
        log.info("Received sync request with payload: {}", payload);
        
        String firebaseUid = payload.get("uid");
        String email = payload.get("email");
        String fullName = payload.get("fullName");
        String avatarUrl = payload.get("avatarUrl");

        if (firebaseUid == null || email == null) {
            log.error("Missing required fields: uid or email");
            return ResponseEntity.badRequest().build();
        }

        try {
            User user = userService.createOrUpdateUser(firebaseUid, email, fullName, avatarUrl);
            log.info("User synchronized successfully: {}", user);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error synchronizing user: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }           
    
    
    
    
    
} 