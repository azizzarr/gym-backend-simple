package com.gymapp.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gymapp.backend.dto.UserProfileDTO;
import com.gymapp.backend.model.enums.HealthCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.Period;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    /**
     * Generates a personalized workout plan based on user profile
     * 
     * @param userProfile The user profile containing fitness goals, preferences, etc.
     * @return A JSON string containing the generated workout plan
     */
    public String generateWorkoutPlan(UserProfileDTO userProfile) {
        log.info("Generating workout plan for user: {}", userProfile.getFirebaseUid());
        
        // Create the prompt for Gemini API
        String prompt = createWorkoutPrompt(userProfile);
        
        // Generate profile description
        String profileDescription = generateProfileDescription(userProfile);
        
        // Prepare the request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", new Object[]{
            Map.of("parts", new Object[]{
                Map.of("text", prompt)
            })
        });
        
        // Make the API call with timeout and retry logic
        String workoutPlanJson = webClientBuilder.build()
                .post()
                .uri(geminiApiUrl + "?key=" + geminiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))  // Add 30-second timeout
                .retry(3)  // Retry up to 3 times
                .doOnError(error -> log.error("Error calling Gemini API: {}", error.getMessage()))
                .block();
        
        // Add profile description to the response
        try {
            JsonNode rootNode = objectMapper.readTree(workoutPlanJson);
            ((ObjectNode) rootNode).put("profileDescription", profileDescription);
            return objectMapper.writeValueAsString(rootNode);
        } catch (Exception e) {
            log.error("Error processing Gemini API response: {}", e.getMessage());
            throw new RuntimeException("Failed to process workout plan response");
        }
    }
    
    /**
     * Creates a detailed prompt for the Gemini API based on user profile
     */
    private String createWorkoutPrompt(UserProfileDTO userProfile) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a concise workout plan for a client with these details:\n\n");
        
        // Essential information only
        prompt.append("Age: ").append(calculateAge(userProfile.getDateOfBirth())).append("\n");
        prompt.append("Gender: ").append(userProfile.getGender()).append("\n");
        prompt.append("Height: ").append(userProfile.getHeight()).append(" cm\n");
        prompt.append("Weight: ").append(userProfile.getCurrentWeight()).append(" kg\n");
        prompt.append("Goal: ").append(userProfile.getFitnessGoals()).append("\n");
        prompt.append("Equipment: ").append(userProfile.getAvailableEquipment()).append("\n");
        prompt.append("Health: ").append(userProfile.getHealthConditions()).append("\n");
        
        // Simplified instructions
        prompt.append("\nGenerate a 3-4 day weekly plan with:\n");
        prompt.append("- Workout type and duration\n");
        prompt.append("- 4-6 exercises per workout\n");
        prompt.append("- Sets, reps, and rest periods\n");
        prompt.append("- Keep notes brief and essential\n");
        prompt.append("- Simple progression plan\n");
        
        // JSON template
        prompt.append("\nFormat as JSON:\n");
        prompt.append("{\n");
        prompt.append("  \"weeklySchedule\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"day\": \"Monday\",\n");
        prompt.append("      \"workoutType\": \"Strength\",\n");
        prompt.append("      \"durationMinutes\": 45,\n");
        prompt.append("      \"exercises\": [\n");
        prompt.append("        {\n");
        prompt.append("          \"name\": \"Exercise Name\",\n");
        prompt.append("          \"sets\": 3,\n");
        prompt.append("          \"reps\": 10,\n");
        prompt.append("          \"restSeconds\": 60,\n");
        prompt.append("          \"notes\": \"Brief form tip\"\n");
        prompt.append("        }\n");
        prompt.append("      ],\n");
        prompt.append("      \"caloriesBurnt\": 300,\n");
        prompt.append("      \"notes\": \"Brief workout focus\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"progressionPlan\": \"Simple 2-week progression\",\n");
        prompt.append("  \"safetyPrecautions\": \"Essential safety notes\"\n");
        prompt.append("}\n");
        
        // Add instructions for keeping notes concise
        prompt.append("\nIMPORTANT: Keep all notes and descriptions brief and essential. Avoid lengthy explanations.\n");
        
        return prompt.toString();
    }
    
    /**
     * Calculates age based on date of birth
     */
    private int calculateAge(java.time.LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return 30; // Default age if not provided
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    /**
     * Generates a descriptive paragraph about the user's profile
     * 
     * @param userProfile The user profile containing fitness goals, preferences, etc.
     * @return A descriptive paragraph about the user's profile
     */
    private String generateProfileDescription(UserProfileDTO userProfile) {
        StringBuilder description = new StringBuilder();
        
        // Calculate age if date of birth is available
        String ageInfo = "";
        if (userProfile.getDateOfBirth() != null) {
            int age = Period.between(userProfile.getDateOfBirth(), LocalDate.now()).getYears();
            ageInfo = String.format(", a %d-year-old", age);
        }
        
        // Basic information
        description.append(String.format("%s%s %s", 
            userProfile.getFirstName(),
            ageInfo,
            userProfile.getGender().toString().toLowerCase()));
        
        // Physical attributes
        if (userProfile.getHeight() != null && userProfile.getCurrentWeight() != null) {
            description.append(String.format(" with a height of %.1f cm and current weight of %.1f kg",
                userProfile.getHeight(),
                userProfile.getCurrentWeight()));
        }
        
        // Fitness goals
        description.append(String.format(". Their primary fitness goal is %s",
            userProfile.getFitnessGoals().toString().toLowerCase().replace("_", " ")));
        
        // Activity level
        description.append(String.format(" and they maintain a %s activity level",
            userProfile.getActivityLevel().toString().toLowerCase().replace("_", " ")));
        
        // Workout preferences
        description.append(String.format(". They prefer to work out %s at %s",
            userProfile.getWorkoutLocations().toString().toLowerCase(),
            userProfile.getWorkoutTimes().toString().toLowerCase()));
        
        // Equipment availability
        description.append(String.format(" and have access to %s equipment",
            userProfile.getAvailableEquipment().toString().toLowerCase().replace("_", " ")));
        
        // Health conditions if any
        if (userProfile.getHealthConditions() != null && userProfile.getHealthConditions() != HealthCondition.NONE) {
            description.append(String.format(". They have %s health conditions",
                userProfile.getHealthConditions().toString().toLowerCase().replace("_", " ")));
        }
        
        // Target weight if set
        if (userProfile.getTargetWeight() != null) {
            description.append(String.format(". Their target weight is %.1f kg",
                userProfile.getTargetWeight()));
        }
        
        return description.toString();
    }
} 