package com.gymapp.backend.controller;

import com.gymapp.backend.dto.WorkoutPlanDTO;
import com.gymapp.backend.dto.WorkoutPlanResponseDTO;
import com.gymapp.backend.service.WorkoutPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/workout-plans")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://gym-app-c37ed.web.app", "https://gym-app-c37ed.firebaseapp.com", "https://gymapp-backend-staging.up.railway.app"}, allowCredentials = "true")
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;

    /**
     * Generates a personalized workout plan for a user
     * 
     * @param firebaseUid The Firebase UID of the user
     * @return The generated workout plan response including the profile description
     */
    @PostMapping("/generate/{firebaseUid}")
    public ResponseEntity<WorkoutPlanResponseDTO> generateWorkoutPlan(@PathVariable String firebaseUid) {
        log.info("Received request to generate workout plan for user: {}", firebaseUid);
        try {
            WorkoutPlanResponseDTO workoutPlanResponse = workoutPlanService.generateAndSaveWorkoutPlan(firebaseUid);
            return ResponseEntity.ok(workoutPlanResponse);
        } catch (RuntimeException e) {
            log.error("Error generating workout plan: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Retrieves the most recently generated workout plan for a user
     * 
     * @param firebaseUid The Firebase UID of the user
     * @return The most recently generated workout plan
     */
    @GetMapping("/{firebaseUid}")
    public ResponseEntity<WorkoutPlanDTO> getLatestWorkoutPlan(@PathVariable String firebaseUid) {
        log.info("Received request to get latest workout plan for user: {}", firebaseUid);
        try {
            WorkoutPlanDTO workoutPlan = workoutPlanService.getLatestWorkoutPlan(firebaseUid);
            if (workoutPlan == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(workoutPlan);
        } catch (RuntimeException e) {
            log.error("Error retrieving workout plan: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
} 