package com.gymapp.backend.dto;

import com.gymapp.backend.model.User;
import com.gymapp.backend.model.UserRole;
import com.gymapp.backend.model.WeightProgress;
import com.gymapp.backend.model.Workout;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserProfileDTO {
    private String firebaseUid;
    private String email;
    private String fullName;
    private UserRole role;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Workout> workouts;
    private List<WeightProgress> weightProgress;
} 