package com.gymapp.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymapp.backend.dto.UserProfileDTO;
import com.gymapp.backend.dto.WorkoutPlanDTO;
import com.gymapp.backend.dto.WorkoutPlanResponseDTO;
import com.gymapp.backend.model.User;
import com.gymapp.backend.model.Workout;
import com.gymapp.backend.repository.UserRepository;
import com.gymapp.backend.repository.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkoutPlanService {

    private final GeminiService geminiService;
    private final UserProfileService userProfileService;
    private final WorkoutService workoutService;
    private final UserRepository userRepository;
    private final WorkoutRepository workoutRepository;
    private final ObjectMapper objectMapper;

    /**
     * Generates a personalized workout plan for a user and saves it to the database
     * 
     * @param firebaseUid The Firebase UID of the user
     * @return The generated workout plan response including the profile description
     */
    @Transactional
    public WorkoutPlanResponseDTO generateAndSaveWorkoutPlan(String firebaseUid) {
        log.info("Generating and saving workout plan for user: {}", firebaseUid);
        
        // Get user profile
        UserProfileDTO userProfile = userProfileService.getProfileByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User profile not found for Firebase UID: " + firebaseUid));
        
        // Generate workout plan using Gemini API
        String workoutPlanJson = geminiService.generateWorkoutPlan(userProfile);
        
        try {
            // Parse the JSON response
            JsonNode rootNode = objectMapper.readTree(workoutPlanJson);
            JsonNode candidatesNode = rootNode.path("candidates");
            
            if (candidatesNode.isArray() && candidatesNode.size() > 0) {
                JsonNode contentNode = candidatesNode.get(0).path("content");
                JsonNode partsNode = contentNode.path("parts");
                
                if (partsNode.isArray() && partsNode.size() > 0) {
                    String textContent = partsNode.get(0).path("text").asText();
                    
                    // Extract the JSON part from the text content
                    String jsonContent = extractJsonFromText(textContent);
                    
                    // Parse the workout plan
                    WorkoutPlanDTO workoutPlan;
                    try {
                        workoutPlan = objectMapper.readValue(jsonContent, WorkoutPlanDTO.class);
                    } catch (JsonProcessingException e) {
                        log.error("Error parsing workout plan JSON: {}", e.getMessage());
                        log.error("Problematic JSON content: {}", jsonContent);
                        
                        // Try to fix common JSON issues
                        jsonContent = fixCommonJsonIssues(jsonContent);
                        
                        try {
                            workoutPlan = objectMapper.readValue(jsonContent, WorkoutPlanDTO.class);
                        } catch (JsonProcessingException e2) {
                            log.error("Error parsing workout plan JSON after fixes: {}", e2.getMessage());
                            throw new RuntimeException("Failed to parse workout plan: " + e2.getMessage());
                        }
                    }
                    
                    // Get the profile description from the root node
                    String profileDescription = rootNode.path("profileDescription").asText();
                    
                    // Save the workout plan to the database
                    saveWorkoutPlanToDatabase(firebaseUid, workoutPlan);
                    
                    // Return the workout plan response with the profile description at the top
                    return WorkoutPlanResponseDTO.builder()
                            .profileDescription(profileDescription)
                            .workoutPlan(workoutPlan)
                            .build();
                }
            }
            
            throw new RuntimeException("Failed to parse workout plan from Gemini API response");
        } catch (JsonProcessingException e) {
            log.error("Error parsing workout plan: {}", e.getMessage());
            throw new RuntimeException("Failed to parse workout plan: " + e.getMessage());
        }
    }
    
    /**
     * Retrieves the most recently generated workout plan for a user
     * 
     * @param firebaseUid The Firebase UID of the user
     * @return The most recently generated workout plan, or null if no workout plan exists
     */
    @Transactional(readOnly = true)
    public WorkoutPlanDTO getLatestWorkoutPlan(String firebaseUid) {
        log.info("Retrieving latest workout plan for user: {}", firebaseUid);
        
        // Get user
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User not found with Firebase UID: " + firebaseUid));
        
        // Get all workouts for the user
        List<Workout> allWorkouts = workoutRepository.findByUserOrderByWorkoutDateDesc(user);
        
        if (allWorkouts.isEmpty()) {
            log.info("No workout plan found for user: {}", firebaseUid);
            return null;
        }
        
        // Create a map to store workouts by day
        Map<String, WorkoutPlanDTO.WorkoutDayDTO> workoutDaysMap = new HashMap<>();
        String progressionPlan = "";
        String safetyPrecautions = "";
        
        // Process each workout
        for (Workout workout : allWorkouts) {
            // Extract day from notes
            String day = "Unknown";
            if (workout.getNotes() != null) {
                String[] noteLines = workout.getNotes().split("\n");
                for (String line : noteLines) {
                    if (line.startsWith("Day:")) {
                        day = line.substring("Day:".length()).trim();
                        break;
                    }
                }
            }
            
            // Skip if we already have a workout for this day
            if (workoutDaysMap.containsKey(day)) {
                continue;
            }
            
            // Extract progression plan and safety precautions from the first workout's notes
            if (progressionPlan.isEmpty() && safetyPrecautions.isEmpty()) {
                String notes = workout.getNotes();
                if (notes != null) {
                    String[] noteLines = notes.split("\n");
                    for (String line : noteLines) {
                        if (line.startsWith("Progression Plan:")) {
                            progressionPlan = line.substring("Progression Plan:".length()).trim();
                        } else if (line.startsWith("Safety Precautions:")) {
                            safetyPrecautions = line.substring("Safety Precautions:".length()).trim();
                        }
                    }
                }
            }
            
            // Create workout day DTO
            WorkoutPlanDTO.WorkoutDayDTO workoutDay = new WorkoutPlanDTO.WorkoutDayDTO();
            workoutDay.setDay(day);
            workoutDay.setWorkoutType(workout.getWorkoutType());
            workoutDay.setDurationMinutes(workout.getDurationMinutes());
            workoutDay.setCaloriesBurnt(workout.getCaloriesBurnt());
            
            // Extract notes
            String notes = "";
            if (workout.getNotes() != null) {
                String[] noteLines = workout.getNotes().split("\n");
                for (String line : noteLines) {
                    if (line.startsWith("Notes:")) {
                        notes = line.substring("Notes:".length()).trim();
                        break;
                    }
                }
            }
            workoutDay.setNotes(notes);
            
            // Parse exercises from JSON
            try {
                if (workout.getExercises() != null && !workout.getExercises().isEmpty()) {
                    List<WorkoutPlanDTO.ExerciseDTO> exercises = objectMapper.readValue(
                            workout.getExercises(),
                            objectMapper.getTypeFactory().constructCollectionType(
                                    List.class, WorkoutPlanDTO.ExerciseDTO.class));
                    workoutDay.setExercises(exercises);
                } else {
                    workoutDay.setExercises(new ArrayList<>());
                }
            } catch (JsonProcessingException e) {
                log.error("Error parsing exercises JSON: {}", e.getMessage());
                workoutDay.setExercises(new ArrayList<>());
            }
            
            // Add to map
            workoutDaysMap.put(day, workoutDay);
            
            // Break if we have all 7 days
            if (workoutDaysMap.size() == 7) {
                break;
            }
        }
        
        // Convert map to list, ensuring days are in correct order
        List<WorkoutPlanDTO.WorkoutDayDTO> workoutDays = new ArrayList<>();
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (String day : daysOfWeek) {
            WorkoutPlanDTO.WorkoutDayDTO workoutDay = workoutDaysMap.get(day);
            if (workoutDay != null) {
                workoutDays.add(workoutDay);
            }
        }
        
        // Create and return the workout plan DTO
        return WorkoutPlanDTO.builder()
                .weeklySchedule(workoutDays)
                .progressionPlan(progressionPlan)
                .safetyPrecautions(safetyPrecautions)
                .build();
    }
    
    /**
     * Extracts the JSON part from the text content returned by Gemini API
     * and preprocesses it to fix common formatting issues
     */
    private String extractJsonFromText(String text) {
        int startIndex = text.indexOf('{');
        int endIndex = text.lastIndexOf('}') + 1;
        
        if (startIndex >= 0 && endIndex > startIndex) {
            String jsonContent = text.substring(startIndex, endIndex);
            
            // Fix the reps format issue (e.g., "8-12" -> "10")
            jsonContent = jsonContent.replaceAll("\"reps\":\\s*\"(\\d+)-(\\d+)\"", "\"reps\": $1");
            
            return jsonContent;
        }
        
        return text;
    }
    
    /**
     * Fixes common JSON issues in the workout plan JSON
     */
    private String fixCommonJsonIssues(String jsonContent) {
        // Fix the reps format issue (e.g., "8-12" -> "10")
        jsonContent = jsonContent.replaceAll("\"reps\":\\s*\"(\\d+)-(\\d+)\"", "\"reps\": $1");
        
        // Fix missing quotes around property names
        jsonContent = jsonContent.replaceAll("([{,])\\s*([a-zA-Z]+):", "$1\"$2\":");
        
        // Fix missing commas between array elements
        jsonContent = jsonContent.replaceAll("}\\s*{", "},{");
        
        // Fix trailing commas in arrays
        jsonContent = jsonContent.replaceAll(",\\s*]", "]");
        
        // Fix trailing commas in objects
        jsonContent = jsonContent.replaceAll(",\\s*}", "}");
        
        return jsonContent;
    }
    
    /**
     * Saves the workout plan to the database as individual workout records
     */
    private void saveWorkoutPlanToDatabase(String firebaseUid, WorkoutPlanDTO workoutPlan) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User not found with Firebase UID: " + firebaseUid));
        
        List<Workout> savedWorkouts = new ArrayList<>();
        
        // Save each workout day as a separate workout record
        for (WorkoutPlanDTO.WorkoutDayDTO workoutDay : workoutPlan.getWeeklySchedule()) {
            Workout workout = new Workout();
            workout.setUser(user);
            
            // Set workout date to the next occurrence of the specified day
            LocalDateTime workoutDate = getNextOccurrenceOfDay(workoutDay.getDay());
            workout.setWorkoutDate(workoutDate);
            
            workout.setWorkoutType(workoutDay.getWorkoutType());
            workout.setDurationMinutes(workoutDay.getDurationMinutes());
            workout.setCaloriesBurnt(workoutDay.getCaloriesBurnt());
            
            // Convert exercises to JSON string
            try {
                String exercisesJson = objectMapper.writeValueAsString(workoutDay.getExercises());
                workout.setExercises(exercisesJson);
            } catch (JsonProcessingException e) {
                log.error("Error converting exercises to JSON: {}", e.getMessage());
                workout.setExercises("[]");
            }
            
            // Set notes
            StringBuilder notes = new StringBuilder();
            notes.append("Day: ").append(workoutDay.getDay()).append("\n");
            notes.append("Notes: ").append(workoutDay.getNotes()).append("\n");
            notes.append("Progression Plan: ").append(workoutPlan.getProgressionPlan()).append("\n");
            notes.append("Safety Precautions: ").append(workoutPlan.getSafetyPrecautions());
            workout.setNotes(notes.toString());
            
            // Save the workout
            Workout savedWorkout = workoutRepository.save(workout);
            savedWorkouts.add(savedWorkout);
        }
        
        log.info("Saved {} workout records for user: {}", savedWorkouts.size(), firebaseUid);
    }
    
    /**
     * Gets the next occurrence of the specified day of the week
     */
    private LocalDateTime getNextOccurrenceOfDay(String day) {
        LocalDateTime now = LocalDateTime.now();
        int targetDay = getDayOfWeekValue(day);
        int currentDay = now.getDayOfWeek().getValue();
        
        int daysToAdd = (targetDay - currentDay + 7) % 7;
        if (daysToAdd == 0) {
            daysToAdd = 7; // If today is the target day, schedule for next week
        }
        
        return now.plusDays(daysToAdd).withHour(9).withMinute(0).withSecond(0).withNano(0);
    }
    
    /**
     * Converts day name to day of week value (1-7, where 1 is Monday)
     */
    private int getDayOfWeekValue(String day) {
        return switch (day.toLowerCase()) {
            case "monday" -> 1;
            case "tuesday" -> 2;
            case "wednesday" -> 3;
            case "thursday" -> 4;
            case "friday" -> 5;
            case "saturday" -> 6;
            case "sunday" -> 7;
            default -> 1; // Default to Monday
        };
    }
} 