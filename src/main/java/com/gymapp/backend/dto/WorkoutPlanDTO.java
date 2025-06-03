package com.gymapp.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutPlanDTO {
    private List<WorkoutDayDTO> weeklySchedule;
    private String progressionPlan;
    private String safetyPrecautions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkoutDayDTO {
        private String day;
        private String workoutType;
        private Integer durationMinutes;
        private List<ExerciseDTO> exercises;
        private Integer caloriesBurnt;
        private String notes;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExerciseDTO {
        private String name;
        private Integer sets;
        private Integer reps;
        private Integer restSeconds;
        private String notes;
    }
} 