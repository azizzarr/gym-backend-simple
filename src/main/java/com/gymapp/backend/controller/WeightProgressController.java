package com.gymapp.backend.controller;

import com.gymapp.backend.model.WeightProgress;
import com.gymapp.backend.service.WeightProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/weight-progress")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://gym-app-angular.vercel.app"})
public class WeightProgressController {
    private final WeightProgressService weightProgressService;

    @PostMapping("/{firebaseUid}")
    public ResponseEntity<WeightProgress> addWeightEntry(
            @PathVariable String firebaseUid,
            @RequestBody WeightProgress weightProgress) {
        log.info("Received request to add weight entry for user: {}", firebaseUid);
        try {
            WeightProgress savedEntry = weightProgressService.addWeightEntry(firebaseUid, weightProgress);
            return ResponseEntity.ok(savedEntry);
        } catch (Exception e) {
            log.error("Error adding weight entry: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{firebaseUid}")
    public ResponseEntity<List<WeightProgress>> getUserWeightHistory(@PathVariable String firebaseUid) {
        log.info("Received request to get weight history for user: {}", firebaseUid);
        try {
            List<WeightProgress> weightHistory = weightProgressService.getUserWeightHistory(firebaseUid);
            return ResponseEntity.ok(weightHistory);
        } catch (Exception e) {
            log.error("Error fetching weight history: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{firebaseUid}/range")
    public ResponseEntity<List<WeightProgress>> getUserWeightHistoryByDateRange(
            @PathVariable String firebaseUid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Received request to get weight history for user: {} between {} and {}", firebaseUid, startDate, endDate);
        try {
            List<WeightProgress> weightHistory = weightProgressService.getUserWeightHistoryByDateRange(firebaseUid, startDate, endDate);
            return ResponseEntity.ok(weightHistory);
        } catch (Exception e) {
            log.error("Error fetching weight history by date range: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{weightEntryId}")
    public ResponseEntity<WeightProgress> updateWeightEntry(
            @PathVariable String weightEntryId,
            @RequestBody WeightProgress updatedWeightProgress) {
        log.info("Received request to update weight entry: {}", weightEntryId);
        try {
            WeightProgress updatedEntry = weightProgressService.updateWeightEntry(
                    java.util.UUID.fromString(weightEntryId),
                    updatedWeightProgress
            );
            return ResponseEntity.ok(updatedEntry);
        } catch (Exception e) {
            log.error("Error updating weight entry: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{weightEntryId}")
    public ResponseEntity<Void> deleteWeightEntry(@PathVariable String weightEntryId) {
        log.info("Received request to delete weight entry: {}", weightEntryId);
        try {
            weightProgressService.deleteWeightEntry(java.util.UUID.fromString(weightEntryId));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting weight entry: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
} 