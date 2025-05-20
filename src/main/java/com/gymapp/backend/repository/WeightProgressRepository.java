package com.gymapp.backend.repository;

import com.gymapp.backend.model.User;
import com.gymapp.backend.model.WeightProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WeightProgressRepository extends JpaRepository<WeightProgress, UUID> {
    List<WeightProgress> findByUserOrderByMeasurementDateDesc(User user);
    List<WeightProgress> findByUserAndMeasurementDateBetweenOrderByMeasurementDateDesc(
        User user, 
        java.time.LocalDateTime startDate, 
        java.time.LocalDateTime endDate
    );
} 