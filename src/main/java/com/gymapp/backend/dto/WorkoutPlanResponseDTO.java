package com.gymapp.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for the response that includes both the workout plan and the profile description.
 * The profile description is not stored in the database but is included in the response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutPlanResponseDTO {
    private String profileDescription;
    private WorkoutPlanDTO workoutPlan;
} 