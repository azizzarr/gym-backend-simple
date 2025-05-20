package com.gymapp.backend.service;

import com.gymapp.backend.dto.UserProfileDTO;
import com.gymapp.backend.model.User;
import com.gymapp.backend.model.UserRole;
import com.gymapp.backend.model.WeightProgress;
import com.gymapp.backend.model.Workout;
import com.gymapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final WorkoutService workoutService;
    private final WeightProgressService weightProgressService;

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

    @Transactional(readOnly = true)
    public User getUserById(UUID id) {
        log.info("Fetching user with ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new RuntimeException("User not found with ID: " + id);
                });
    }

    @Transactional(readOnly = true)
    public User getUserByFirebaseUid(String firebaseUid) {
        log.info("Fetching user with Firebase UID: {}", firebaseUid);
        return userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> {
                    log.error("User not found with Firebase UID: {}", firebaseUid);
                    return new RuntimeException("User not found with Firebase UID: " + firebaseUid);
                });
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(String firebaseUid) {
        log.info("Fetching complete profile for user with Firebase UID: {}", firebaseUid);
        
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User not found with Firebase UID: " + firebaseUid));
        
        List<Workout> workouts = workoutService.getUserWorkouts(firebaseUid);
        List<WeightProgress> weightProgress = weightProgressService.getUserWeightHistory(firebaseUid);
        
        return UserProfileDTO.builder()
                .firebaseUid(user.getFirebaseUid())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .workouts(workouts)
                .weightProgress(weightProgress)
                .build();
    }
} 