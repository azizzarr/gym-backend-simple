package com.gymapp.backend.dto;

import com.gymapp.backend.model.User;
import com.gymapp.backend.model.UserRole;
import com.gymapp.backend.model.WeightProgress;
import com.gymapp.backend.model.Workout;
import com.gymapp.backend.model.enums.ActivityLevel;
import com.gymapp.backend.model.enums.Equipment;
import com.gymapp.backend.model.enums.FitnessGoal;
import com.gymapp.backend.model.enums.Gender;
import com.gymapp.backend.model.enums.HealthCondition;
import com.gymapp.backend.model.enums.WorkoutLocation;
import com.gymapp.backend.model.enums.WorkoutTime;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserProfileDTO {
    // Basic User Information
    private String firebaseUid;
    private String email;
    private String fullName;
    private UserRole role;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Personal Information
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Gender gender;

    // Physical Information
    private Double height; // in cm
    private Double currentWeight; // in kg
    private Double targetWeight; // in kg
    private ActivityLevel activityLevel;

    // Fitness Goals
    private FitnessGoal fitnessGoals;

    // Workout Preferences
    private WorkoutLocation workoutLocations;
    private WorkoutTime workoutTimes;
    private Equipment availableEquipment;

    // Health Information
    private HealthCondition healthConditions;
    private String otherHealthCondition;

    // Progress Tracking
    private List<Workout> workouts;
    private List<WeightProgress> weightProgress;
} 