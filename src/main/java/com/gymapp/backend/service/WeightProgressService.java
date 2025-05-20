package com.gymapp.backend.service;

import com.gymapp.backend.model.User;
import com.gymapp.backend.model.WeightProgress;
import com.gymapp.backend.repository.UserRepository;
import com.gymapp.backend.repository.WeightProgressRepository;
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
public class WeightProgressService {
    private final WeightProgressRepository weightProgressRepository;
    private final UserRepository userRepository;

    @Transactional
    public WeightProgress addWeightEntry(String firebaseUid, WeightProgress weightProgress) {
        log.info("Adding weight entry for user with Firebase UID: {}", firebaseUid);
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User not found with Firebase UID: " + firebaseUid));
        
        weightProgress.setUser(user);
        weightProgress.setCreatedAt(LocalDateTime.now());
        weightProgress.setUpdatedAt(LocalDateTime.now());
        
        return weightProgressRepository.save(weightProgress);
    }

    @Transactional(readOnly = true)
    public List<WeightProgress> getUserWeightHistory(String firebaseUid) {
        log.info("Fetching weight history for user with Firebase UID: {}", firebaseUid);
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User not found with Firebase UID: " + firebaseUid));
        
        return weightProgressRepository.findByUserOrderByMeasurementDateDesc(user);
    }

    @Transactional(readOnly = true)
    public List<WeightProgress> getUserWeightHistoryByDateRange(String firebaseUid, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching weight history for user with Firebase UID: {} between {} and {}", firebaseUid, startDate, endDate);
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User not found with Firebase UID: " + firebaseUid));
        
        return weightProgressRepository.findByUserAndMeasurementDateBetweenOrderByMeasurementDateDesc(user, startDate, endDate);
    }

    @Transactional
    public WeightProgress updateWeightEntry(UUID weightEntryId, WeightProgress updatedWeightProgress) {
        log.info("Updating weight entry with ID: {}", weightEntryId);
        WeightProgress existingEntry = weightProgressRepository.findById(weightEntryId)
                .orElseThrow(() -> new RuntimeException("Weight entry not found with ID: " + weightEntryId));
        
        // Update fields
        existingEntry.setWeightKg(updatedWeightProgress.getWeightKg());
        existingEntry.setMeasurementDate(updatedWeightProgress.getMeasurementDate());
        existingEntry.setPictureUrl(updatedWeightProgress.getPictureUrl());
        existingEntry.setNotes(updatedWeightProgress.getNotes());
        existingEntry.setUpdatedAt(LocalDateTime.now());
        
        return weightProgressRepository.save(existingEntry);
    }

    @Transactional
    public void deleteWeightEntry(UUID weightEntryId) {
        log.info("Deleting weight entry with ID: {}", weightEntryId);
        if (!weightProgressRepository.existsById(weightEntryId)) {
            throw new RuntimeException("Weight entry not found with ID: " + weightEntryId);
        }
        weightProgressRepository.deleteById(weightEntryId);
    }
} 