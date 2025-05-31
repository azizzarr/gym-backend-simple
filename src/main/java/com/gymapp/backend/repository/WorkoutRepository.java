package com.gymapp.backend.repository;

import com.gymapp.backend.model.User;
import com.gymapp.backend.model.Workout;
import org.springframework.data.domain.Pageable;
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
    
    /**
     * Finds the most recent workouts for a user, limited to the specified number
     * 
     * @param user The user
     * @param pageable The pageable object to limit the results
     * @return A list of the most recent workouts
     */
    List<Workout> findByUserOrderByWorkoutDateDesc(User user, Pageable pageable);
    
    /**
     * Finds the top 7 most recent workouts for a user
     * 
     * @param user The user
     * @return A list of the top 7 most recent workouts
     */
    default List<Workout> findTop7ByUserOrderByWorkoutDateDesc(User user) {
        return findByUserOrderByWorkoutDateDesc(user, Pageable.ofSize(7));
    }

    /**
     * Finds all prebuilt workouts
     * 
     * @return A list of all prebuilt workouts
     */
    List<Workout> findByPrebuiltTrue();
} 