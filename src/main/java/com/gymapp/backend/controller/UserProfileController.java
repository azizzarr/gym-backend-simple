package com.gymapp.backend.controller;

import com.gymapp.backend.dto.UserProfileDTO;
import com.gymapp.backend.model.UserProfile;
import com.gymapp.backend.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {
    private final UserProfileService userProfileService;

    @PostMapping("/{firebaseUid}")
    public ResponseEntity<UserProfile> createProfile(@PathVariable String firebaseUid, @RequestBody UserProfileDTO dto) {
        dto.setFirebaseUid(firebaseUid);
        UserProfile created = userProfileService.createProfile(dto);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{firebaseUid}")
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable String firebaseUid) {
        log.info("Received request to get profile for user: {}", firebaseUid);
        try {
            UserProfileDTO profile = userProfileService.getProfileByFirebaseUid(firebaseUid)
                    .orElseThrow(() -> new RuntimeException("Profile not found"));
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Error fetching user profile: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
} 