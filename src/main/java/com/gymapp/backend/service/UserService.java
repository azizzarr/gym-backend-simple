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
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseGet(() -> User.builder()
                        .firebaseUid(firebaseUid)
                        .email(email)
                        .fullName(fullName)
                        .avatarUrl(avatarUrl)
                        .role(UserRole.CLIENT)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());

        // Update user details if they exist
        user.setEmail(email);
        user.setFullName(fullName);
        user.setAvatarUrl(avatarUrl);
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }
} 