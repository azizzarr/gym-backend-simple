package com.gymapp.backend.service;

import com.gymapp.backend.model.User;
import com.gymapp.backend.model.UserRole;
import com.gymapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public User createOrUpdateUser(String firebaseUid, String email, String fullName, String avatarUrl) {
        log.debug("Creating or updating user with firebaseUid: {}, email: {}", firebaseUid, email);
        
        try {
            User user = userRepository.findByFirebaseUid(firebaseUid)
                    .orElseGet(() -> {
                        log.info("Creating new user with email: {}", email);
                        return User.builder()
                                .firebaseUid(firebaseUid)
                                .email(email)
                                .fullName(fullName)
                                .avatarUrl(avatarUrl)
                                .role(UserRole.CLIENT)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
                    });

            // Update user details if they exist
            user.setEmail(email);
            user.setFullName(fullName);
            user.setAvatarUrl(avatarUrl);
            user.setUpdatedAt(LocalDateTime.now());
            
            log.debug("Saving user with email: {}", email);
            return userRepository.save(user);
        } catch (Exception e) {
            log.error("Error creating or updating user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create or update user: " + e.getMessage(), e);
        }
    }
} 