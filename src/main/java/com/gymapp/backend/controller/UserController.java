package com.gymapp.backend.controller;

import com.gymapp.backend.dto.UserProfileDTO;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://gym-app-c37ed.web.app", "https://gym-app-c37ed.firebaseapp.com", "https://gymapp-backend-staging.up.railway.app"}, allowCredentials = "true")
public class UserController {
    private final UserService userService;

    @PostMapping("/sync")
    public ResponseEntity<User> syncUser(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        log.info("Received sync request with method: {} and payload: {}", request.getMethod(), payload);
        
        // Log headers for debugging
        Collections.list(request.getHeaderNames())
            .forEach(header -> log.info("Header: {} = {}", header, request.getHeader(header)));
        
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

    @GetMapping("/firebase/{firebaseUid}")
    public ResponseEntity<User> getUserByFirebaseUid(@PathVariable String firebaseUid) {
        log.info("Received request to get user with Firebase UID: {}", firebaseUid);
        try {
            User user = userService.getUserByFirebaseUid(firebaseUid);
            log.info("User found: {}", user);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            log.error("Error fetching user: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        log.info("Received request to get user with ID: {}", id);
        try {
            User user = userService.getUserById(id);
            log.info("User found: {}", user);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            log.error("Error fetching user: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/profile/{firebaseUid}")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable String firebaseUid) {
        log.info("Received request to get complete profile for user: {}", firebaseUid);
        try {
            UserProfileDTO userProfile = userService.getUserProfile(firebaseUid);
            return ResponseEntity.ok(userProfile);
        } catch (Exception e) {
            log.error("Error fetching user profile: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
} 