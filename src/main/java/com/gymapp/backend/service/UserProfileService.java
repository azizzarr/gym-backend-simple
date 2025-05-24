package com.gymapp.backend.service;

import com.gymapp.backend.dto.UserProfileDTO;
import com.gymapp.backend.model.User;
import com.gymapp.backend.model.UserProfile;
import com.gymapp.backend.model.UserRole;
import com.gymapp.backend.repository.UserProfileRepository;
import com.gymapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserProfile createProfile(UserProfileDTO dto) {
        Optional<User> userOpt = userRepository.findByFirebaseUid(dto.getFirebaseUid());
        User user = userOpt.orElseThrow(() -> new IllegalArgumentException("User not found for firebaseUid: " + dto.getFirebaseUid()));
        
        // Set role as CLIENT automatically
        user.setRole(UserRole.CLIENT);
        userRepository.save(user);
        
        // Always create a new profile for testing purposes
        UserProfile profile = UserProfile.builder()
                .user(user)
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .heightCm(dto.getHeight())
                .currentWeightKg(dto.getCurrentWeight())
                .targetWeightKg(dto.getTargetWeight())
                .activityLevel(dto.getActivityLevel())
                .fitnessGoals(dto.getFitnessGoals())
                .workoutLocations(dto.getWorkoutLocations())
                .workoutTimes(dto.getWorkoutTimes())
                .equipment(dto.getAvailableEquipment())
                .healthConditions(dto.getHealthConditions())
                .otherHealthCondition(dto.getOtherHealthCondition())
                .build();

        // Save the profile
        UserProfile savedProfile = userProfileRepository.save(profile);
        
        // Force a flush to ensure everything is saved
        userProfileRepository.flush();
        
        return savedProfile;
    }

    @Transactional(readOnly = true)
    public Optional<UserProfileDTO> getProfileByFirebaseUid(String firebaseUid) {
        log.info("Fetching profile for user with Firebase UID: {}", firebaseUid);
        return userProfileRepository.findByFirebaseUid(firebaseUid)
                .map(profile -> UserProfileDTO.builder()
                        .firebaseUid(profile.getUser().getFirebaseUid())
                        .dateOfBirth(profile.getDateOfBirth())
                        .gender(profile.getGender())
                        .height(profile.getHeightCm())
                        .currentWeight(profile.getCurrentWeightKg())
                        .targetWeight(profile.getTargetWeightKg())
                        .activityLevel(profile.getActivityLevel())
                        .fitnessGoals(profile.getFitnessGoals())
                        .workoutLocations(profile.getWorkoutLocations())
                        .workoutTimes(profile.getWorkoutTimes())
                        .availableEquipment(profile.getEquipment())
                        .healthConditions(profile.getHealthConditions())
                        .otherHealthCondition(profile.getOtherHealthCondition())
                        .build());
    }
} 