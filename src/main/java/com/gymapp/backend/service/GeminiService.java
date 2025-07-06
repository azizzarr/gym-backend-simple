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
        
        // Make the API call
        String workoutPlanJson = webClientBuilder.build()
                .post()
                .uri(geminiApiUrl + "?key=" + geminiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> log.error("Error calling Gemini API: {}", error.getMessage()))
                .block();
        
        // Add profile description to the response
        try {
            JsonNode rootNode = objectMapper.readTree(workoutPlanJson);
            ((ObjectNode) rootNode).put("profileDescription", profileDescription);
            
            // Format the response to ensure profileDescription is at the top
            ObjectNode formattedResponse = objectMapper.createObjectNode();
            formattedResponse.put("profileDescription", profileDescription);
            
            // Add the rest of the response
            if (rootNode.has("candidates")) {
                formattedResponse.set("candidates", rootNode.get("candidates"));
            }
            
            return objectMapper.writeValueAsString(formattedResponse);
        } catch (Exception e) {
            log.error("Error adding profile description to response: {}", e.getMessage());
            return workoutPlanJson;
        }
    }
    
    /**
     * Creates a detailed prompt for the Gemini API based on user profile
     */
    private String createWorkoutPrompt(UserProfileDTO userProfile) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert fitness trainer with 15+ years of experience creating personalized workout plans. ");
        prompt.append("Your task is to generate a highly detailed, scientifically-backed, and practical workout plan for a client with the following profile:\n\n");
        
        // Add personal information
        prompt.append("PERSONAL INFORMATION:\n");
        int age = calculateAge(userProfile.getDateOfBirth());
        prompt.append("- Age: ").append(age).append(" years\n");
        prompt.append("- Gender: ").append(userProfile.getGender()).append("\n");
        
        // Add physical information
        prompt.append("\nPHYSICAL INFORMATION:\n");
        prompt.append("- Height: ").append(userProfile.getHeight()).append(" cm\n");
        prompt.append("- Current Weight: ").append(userProfile.getCurrentWeight()).append(" kg\n");
        prompt.append("- Target Weight: ").append(userProfile.getTargetWeight()).append(" kg\n");
        prompt.append("- Activity Level: ").append(userProfile.getActivityLevel()).append("\n");
        
        // Add fitness goals
        prompt.append("\nFITNESS GOALS:\n");
        prompt.append("- Primary Goal: ").append(userProfile.getFitnessGoals()).append("\n");
        
        // Add workout preferences
        prompt.append("\nWORKOUT PREFERENCES:\n");
        prompt.append("- Preferred Location: ").append(userProfile.getWorkoutLocations()).append("\n");
        prompt.append("- Preferred Time: ").append(userProfile.getWorkoutTimes()).append("\n");
        prompt.append("- Available Equipment: ").append(userProfile.getAvailableEquipment()).append("\n");
        
        // Add health information
        prompt.append("\nHEALTH INFORMATION:\n");
        prompt.append("- Health Conditions: ").append(userProfile.getHealthConditions()).append("\n");
        if (userProfile.getOtherHealthCondition() != null && !userProfile.getOtherHealthCondition().isEmpty()) {
            prompt.append("- Other Health Conditions: ").append(userProfile.getOtherHealthCondition()).append("\n");
        }
        
        // Add specific instructions for the workout plan
        prompt.append("\nINSTRUCTIONS FOR WORKOUT PLAN GENERATION:\n");
        prompt.append("1. Create a weekly schedule with 3-5 workout days, considering the client's fitness level and goals.\n");
        prompt.append("2. For each workout day, provide:\n");
        prompt.append("   - Specific workout type (strength, cardio, HIIT, flexibility, etc.)\n");
        prompt.append("   - Duration in minutes (30-60 minutes is typical)\n");
        prompt.append("   - Detailed list of exercises with:\n");
        prompt.append("     * Exercise name (use standard, widely recognized exercise names)\n");
        prompt.append("     * Sets (typically 2-4 sets)\n");
        prompt.append("     * Reps (typically 8-15 reps for strength, 15-20 for endurance)\n");
        prompt.append("     * Rest periods between sets (in seconds, typically 30-90 seconds)\n");
        prompt.append("     * Specific form instructions and technique cues\n");
        prompt.append("   - Estimated calories burned (based on client's weight and workout intensity)\n");
        prompt.append("   - Notes on workout focus and purpose\n");
        prompt.append("3. Include a 4-week progression plan that gradually increases intensity\n");
        prompt.append("4. Provide general safety precautions based on the client's health conditions and keep it short and concise\n");
        prompt.append("5. Ensure exercises are appropriate for the client's available equipment\n");
        prompt.append("6. Consider the client's age, fitness level, and any health conditions when designing exercises\n");
        prompt.append("7. Include a mix of compound and isolation exercises for balanced development\n");
        prompt.append("8. For strength training, follow proper exercise order (compound before isolation)\n");
        prompt.append("9. Include proper warm-up and cool-down recommendations\n");
        
        // Add specific guidance based on fitness goals
        prompt.append("\nSPECIFIC GUIDANCE BASED ON FITNESS GOALS:\n");
        if (userProfile.getFitnessGoals() != null) {
            switch (userProfile.getFitnessGoals().toString()) {
                case "WEIGHT_LOSS":
                    prompt.append("- Focus on calorie-burning exercises and moderate-intensity cardio\n");
                    prompt.append("- Include HIIT workouts 1-2 times per week\n");
                    prompt.append("- Emphasize compound movements to maximize calorie burn\n");
                    prompt.append("- Include strength training to preserve muscle mass during weight loss\n");
                    break;
                case "MUSCLE_GAIN":
                    prompt.append("- Focus on progressive overload with compound movements\n");
                    prompt.append("- Include adequate rest between sets (60-90 seconds)\n");
                    prompt.append("- Emphasize proper form and controlled movements\n");
                    prompt.append("- Include sufficient protein intake recommendations\n");
                    break;
                case "ENDURANCE":
                    prompt.append("- Focus on cardiovascular exercises and circuit training\n");
                    prompt.append("- Include interval training to improve aerobic capacity\n");
                    prompt.append("- Emphasize proper breathing techniques\n");
                    prompt.append("- Gradually increase workout duration over time\n");
                    break;
                case "FLEXIBILITY":
                    prompt.append("- Focus on dynamic and static stretching\n");
                    prompt.append("- Include yoga or mobility exercises\n");
                    prompt.append("- Emphasize proper breathing during stretches\n");
                    prompt.append("- Include foam rolling recommendations\n");
                    break;
                case "GENERAL_FITNESS":
                    prompt.append("- Include a balanced mix of strength, cardio, and flexibility\n");
                    prompt.append("- Focus on full-body workouts\n");
                    prompt.append("- Emphasize proper form and technique\n");
                    prompt.append("- Include variety to prevent plateaus\n");
                    break;
                default:
                    prompt.append("- Create a balanced workout plan that addresses multiple fitness aspects\n");
                    prompt.append("- Focus on proper form and technique\n");
                    prompt.append("- Include variety to prevent plateaus\n");
                    break;
            }
        }
        
        // Add specific guidance based on available equipment
        prompt.append("\nEQUIPMENT-SPECIFIC GUIDANCE:\n");
        if (userProfile.getAvailableEquipment() != null) {
            switch (userProfile.getAvailableEquipment().toString()) {
                case "FULL_GYM":
                    prompt.append("- Utilize a wide variety of machines, free weights, and cardio equipment\n");
                    prompt.append("- Include both machine-based and free weight exercises\n");
                    prompt.append("- Take advantage of specialized equipment for targeted muscle groups\n");
                    break;
                case "HOME_GYM":
                    prompt.append("- Focus on exercises that can be done with basic equipment (dumbbells, resistance bands, etc.)\n");
                    prompt.append("- Include bodyweight exercises as alternatives to machine exercises\n");
                    prompt.append("- Utilize multi-purpose equipment for multiple muscle groups\n");
                    break;
                case "BODYWEIGHT_ONLY":
                    prompt.append("- Focus on bodyweight exercises that can be done anywhere\n");
                    prompt.append("- Include variations of push-ups, squats, lunges, and planks\n");
                    prompt.append("- Utilize furniture and household items for additional resistance\n");
                    break;
                case "MINIMAL_EQUIPMENT":
                    prompt.append("- Focus on exercises that can be done with minimal equipment (dumbbells, resistance bands, etc.)\n");
                    prompt.append("- Include bodyweight exercises as alternatives to machine exercises\n");
                    prompt.append("- Utilize multi-purpose equipment for multiple muscle groups\n");
                    break;
                default:
                    prompt.append("- Create a workout plan that can be adapted to various equipment setups\n");
                    prompt.append("- Include alternatives for exercises that require specific equipment\n");
                    break;
            }
        }
        
        // Add specific guidance based on health conditions
        prompt.append("\nHEALTH-CONDITION SPECIFIC GUIDANCE:\n");
        if (userProfile.getHealthConditions() != null) {
            switch (userProfile.getHealthConditions().toString()) {
                case "NONE":
                    prompt.append("- No specific modifications needed based on health conditions\n");
                    break;
                case "JOINT_PAIN":
                    prompt.append("- Avoid high-impact exercises that may aggravate joints\n");
                    prompt.append("- Focus on low-impact cardio options (swimming, cycling, etc.)\n");
                    prompt.append("- Include joint-friendly strength exercises\n");
                    prompt.append("- Emphasize proper form to prevent further injury\n");
                    break;
                case "HEART_CONDITION":
                    prompt.append("- Focus on low to moderate intensity exercises\n");
                    prompt.append("- Avoid high-intensity interval training\n");
                    prompt.append("- Include adequate rest periods between exercises\n");
                    prompt.append("- Emphasize proper breathing techniques\n");
                    break;
                case "BACK_PAIN":
                    prompt.append("- Avoid exercises that may aggravate the back\n");
                    prompt.append("- Focus on core strengthening exercises\n");
                    prompt.append("- Include proper form cues for back protection\n");
                    prompt.append("- Emphasize proper posture during exercises\n");
                    break;
                case "DIABETES":
                    prompt.append("- Include both aerobic and resistance training\n");
                    prompt.append("- Focus on consistent, moderate-intensity exercise\n");
                    prompt.append("- Include proper hydration recommendations\n");
                    prompt.append("- Emphasize the importance of monitoring blood sugar levels\n");
                    break;
                case "HYPERTENSION":
                    prompt.append("- Focus on moderate-intensity aerobic exercise\n");
                    prompt.append("- Avoid heavy lifting and high-intensity exercises\n");
                    prompt.append("- Include proper breathing techniques\n");
                    prompt.append("- Emphasize the importance of monitoring blood pressure\n");
                    break;
                default:
                    prompt.append("- Create a workout plan that is safe and appropriate for the client's health conditions\n");
                    prompt.append("- Include modifications for exercises that may be contraindicated\n");
                    prompt.append("- Emphasize proper form and technique to prevent injury\n");
                    break;
            }
        }
        
        // Add specific guidance based on age
        prompt.append("\nAGE-SPECIFIC GUIDANCE:\n");
        if (age < 18) {
            prompt.append("- Focus on proper form and technique\n");
            prompt.append("- Avoid heavy lifting and high-intensity exercises\n");
            prompt.append("- Include a variety of activities to develop overall fitness\n");
            prompt.append("- Emphasize the importance of proper nutrition for growth and development\n");
        } else if (age >= 18 && age < 30) {
            prompt.append("- Focus on building a solid foundation of strength and fitness\n");
            prompt.append("- Include a variety of exercises to target all major muscle groups\n");
            prompt.append("- Emphasize proper form and technique to prevent injury\n");
            prompt.append("- Include high-intensity options for maximum results\n");
        } else if (age >= 30 && age < 50) {
            prompt.append("- Focus on maintaining strength and flexibility\n");
            prompt.append("- Include exercises to prevent age-related muscle loss\n");
            prompt.append("- Emphasize proper form and technique to prevent injury\n");
            prompt.append("- Include stress-reducing activities like yoga or meditation\n");
        } else if (age >= 50) {
            prompt.append("- Focus on maintaining mobility and functional strength\n");
            prompt.append("- Include exercises to prevent falls and improve balance\n");
            prompt.append("- Emphasize proper form and technique to prevent injury\n");
            prompt.append("- Include low-impact options for cardiovascular health\n");
        }
        
        // Add format instructions
        prompt.append("\nFORMAT THE RESPONSE AS A JSON OBJECT WITH THE FOLLOWING STRUCTURE:\n");
        prompt.append("{\n");
        prompt.append("  \"weeklySchedule\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"day\": \"Monday\",\n");
        prompt.append("      \"workoutType\": \"strength\",\n");
        prompt.append("      \"durationMinutes\": 60,\n");
        prompt.append("      \"exercises\": [\n");
        prompt.append("        {\n");
        prompt.append("          \"name\": \"Exercise Name\",\n");
        prompt.append("          \"sets\": 3,\n");
        prompt.append("          \"reps\": 12,\n");
        prompt.append("          \"restSeconds\": 60,\n");
        prompt.append("          \"notes\": \"Form instructions\"\n");
        prompt.append("        }\n");
        prompt.append("      ],\n");
        prompt.append("      \"caloriesBurnt\": 300,\n");
        prompt.append("      \"notes\": \"General notes about the workout\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"progressionPlan\": \"How to progress over the next 4 weeks\",\n");
        prompt.append("  \"safetyPrecautions\": \"Any safety precautions to follow\"\n");
        prompt.append("}\n");
        
        // Add final instructions
        prompt.append("\nIMPORTANT INSTRUCTIONS:\n");
        prompt.append("1. Don't be specific and detailed in your exercise descriptions\n");
        prompt.append("2. Use standard, widely recognized exercise names\n");
        prompt.append("3. Provide realistic sets, reps, and rest periods\n");
        prompt.append("4. IMPORTANT: The 'reps' field must be a single integer value, not a range (e.g., use 10 instead of 8-12)\n");
        prompt.append("5. Include proper form instructions for each exercise but keep it short and concise\n");
        prompt.append("6. Consider the client's fitness level, goals, and limitations\n");
        prompt.append("7. Ensure the workout plan is safe and appropriate for the client\n");
        prompt.append("8. Format the response as a valid JSON object\n");
        prompt.append("9. Do not include any explanatory text outside the JSON structure\n");
        
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