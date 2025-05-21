package com.gymapp.backend.repository;

import com.gymapp.backend.model.User;
import com.gymapp.backend.model.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, UUID> {
    List<Workout> findByUserOrderByWorkoutDateDesc(User user);
    List<Workout> findByUserAndWorkoutDateBetweenOrderByWorkoutDateDesc(
        User user, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
} 