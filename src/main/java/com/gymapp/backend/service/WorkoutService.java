package com.gymapp.backend.service;

import com.gymapp.backend.model.User;
import com.gymapp.backend.model.Workout;
import com.gymapp.backend.repository.UserRepository;
import com.gymapp.backend.repository.WorkoutRepository;
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
public class WorkoutService {
    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;

    @Transactional
    public Workout createWorkout(String firebaseUid, Workout workout) {
        log.info("Creating workout for user with Firebase UID: {}", firebaseUid);
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User not found with Firebase UID: " + firebaseUid));
        
        workout.setUser(user);
        workout.setCreatedAt(LocalDateTime.now());
        workout.setUpdatedAt(LocalDateTime.now());
        
        return workoutRepository.save(workout);
    }

    @Transactional(readOnly = true)
    public List<Workout> getUserWorkouts(String firebaseUid) {
        log.info("Fetching workouts for user with Firebase UID: {}", firebaseUid);
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User not found with Firebase UID: " + firebaseUid));
        
        return workoutRepository.findByUserOrderByWorkoutDateDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Workout> getUserWorkoutsByDateRange(String firebaseUid, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching workouts for user with Firebase UID: {} between {} and {}", firebaseUid, startDate, endDate);
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User not found with Firebase UID: " + firebaseUid));
        
        return workoutRepository.findByUserAndWorkoutDateBetweenOrderByWorkoutDateDesc(user, startDate, endDate);
    }

    @Transactional
    public Workout updateWorkout(UUID workoutId, Workout updatedWorkout) {
        log.info("Updating workout with ID: {}", workoutId);
        Workout existingWorkout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Workout not found with ID: " + workoutId));
        
        // Update fields
        existingWorkout.setWorkoutDate(updatedWorkout.getWorkoutDate());
        existingWorkout.setWorkoutType(updatedWorkout.getWorkoutType());
        existingWorkout.setDurationMinutes(updatedWorkout.getDurationMinutes());
        existingWorkout.setExercises(updatedWorkout.getExercises());
        existingWorkout.setNotes(updatedWorkout.getNotes());
        existingWorkout.setCaloriesBurnt(updatedWorkout.getCaloriesBurnt());
        existingWorkout.setUpdatedAt(LocalDateTime.now());
        
        return workoutRepository.save(existingWorkout);
    }

    @Transactional
    public void deleteWorkout(UUID workoutId) {
        log.info("Deleting workout with ID: {}", workoutId);
        if (!workoutRepository.existsById(workoutId)) {
            throw new RuntimeException("Workout not found with ID: " + workoutId);
        }
        workoutRepository.deleteById(workoutId);
    }

    /**
     * Retrieves all prebuilt workouts
     * 
     * @return A list of all prebuilt workouts
     */
    @Transactional(readOnly = true)
    public List<Workout> getPrebuiltWorkouts() {
        log.info("Fetching all prebuilt workouts");
        return workoutRepository.findByPrebuiltTrue();
    }
} 