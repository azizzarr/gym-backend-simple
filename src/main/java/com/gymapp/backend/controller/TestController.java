package com.gymapp.backend.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
} 