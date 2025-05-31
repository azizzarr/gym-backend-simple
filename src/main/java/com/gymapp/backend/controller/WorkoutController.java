package com.gymapp.backend.controller;

import com.gymapp.backend.model.Workout;
import com.gymapp.backend.service.WorkoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/workouts")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://gym-app-c37ed.web.app", "https://gym-app-c37ed.firebaseapp.com", "https://gymapp-backend-staging.up.railway.app"}, allowCredentials = "true")
public class WorkoutController {
    private final WorkoutService workoutService;

    @PostMapping("/{firebaseUid}")
    public ResponseEntity<Workout> createWorkout(
            @PathVariable String firebaseUid,
            @RequestBody Workout workout) {
        log.info("Received request to create workout for user: {}", firebaseUid);
        try {
            Workout createdWorkout = workoutService.createWorkout(firebaseUid, workout);
            return ResponseEntity.ok(createdWorkout);
        } catch (RuntimeException e) {
            log.error("Error creating workout: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{firebaseUid}")
    public ResponseEntity<List<Workout>> getUserWorkouts(@PathVariable String firebaseUid) {
        log.info("Received request to get workouts for user: {}", firebaseUid);
        try {
            List<Workout> workouts = workoutService.getUserWorkouts(firebaseUid);
            return ResponseEntity.ok(workouts);
        } catch (RuntimeException e) {
            log.error("Error fetching workouts: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{firebaseUid}/range")
    public ResponseEntity<List<Workout>> getUserWorkoutsByDateRange(
            @PathVariable String firebaseUid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Received request to get workouts for user: {} between {} and {}", firebaseUid, startDate, endDate);
        try {
            List<Workout> workouts = workoutService.getUserWorkoutsByDateRange(firebaseUid, startDate, endDate);
            return ResponseEntity.ok(workouts);
        } catch (RuntimeException e) {
            log.error("Error fetching workouts: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{workoutId}")
    public ResponseEntity<Workout> updateWorkout(
            @PathVariable UUID workoutId,
            @RequestBody Workout updatedWorkout) {
        log.info("Received request to update workout: {}", workoutId);
        try {
            Workout workout = workoutService.updateWorkout(workoutId, updatedWorkout);
            return ResponseEntity.ok(workout);
        } catch (RuntimeException e) {
            log.error("Error updating workout: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{workoutId}")
    public ResponseEntity<Void> deleteWorkout(@PathVariable UUID workoutId) {
        log.info("Received request to delete workout: {}", workoutId);
        try {
            workoutService.deleteWorkout(workoutId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("Error deleting workout: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Retrieves all prebuilt workouts
     * 
     * @return A list of all prebuilt workouts
     */
    @GetMapping("/prebuilt")
    public ResponseEntity<List<Workout>> getPrebuiltWorkouts() {
        log.info("Received request to get prebuilt workouts");
        try {
            List<Workout> workouts = workoutService.getPrebuiltWorkouts();
            return ResponseEntity.ok(workouts);
        } catch (RuntimeException e) {
            log.error("Error fetching prebuilt workouts: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
} 