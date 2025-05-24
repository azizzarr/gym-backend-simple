package com.gymapp.backend.model;

import com.gymapp.backend.model.enums.ActivityLevel;
import com.gymapp.backend.model.enums.Equipment;
import com.gymapp.backend.model.enums.FitnessGoal;
import com.gymapp.backend.model.enums.Gender;
import com.gymapp.backend.model.enums.HealthCondition;
import com.gymapp.backend.model.enums.WorkoutLocation;
import com.gymapp.backend.model.enums.WorkoutTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_profiles", schema = "public")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "height_cm")
    private Double heightCm;

    @Column(name = "current_weight_kg")
    private Double currentWeightKg;

    @Column(name = "target_weight_kg")
    private Double targetWeightKg;

    @Column(name = "activity_level", nullable = false)
    @Enumerated(EnumType.STRING)
    private ActivityLevel activityLevel;

    @Column(name = "fitness_goals", nullable = false)
    @Enumerated(EnumType.STRING)
    private FitnessGoal fitnessGoals;

    @Column(name = "workout_locations", nullable = false)
    @Enumerated(EnumType.STRING)
    private WorkoutLocation workoutLocations;

    @Column(name = "workout_times", nullable = false)
    @Enumerated(EnumType.STRING)
    private WorkoutTime workoutTimes;

    @Column(name = "equipment", nullable = false)
    @Enumerated(EnumType.STRING)
    private Equipment equipment;

    @Column(name = "health_conditions", nullable = false)
    @Enumerated(EnumType.STRING)
    private HealthCondition healthConditions;

    @Column(name = "other_health_condition")
    private String otherHealthCondition;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 