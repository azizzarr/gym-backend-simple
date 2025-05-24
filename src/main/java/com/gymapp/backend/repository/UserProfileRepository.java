package com.gymapp.backend.repository;

import com.gymapp.backend.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    @Query("SELECT up FROM UserProfile up WHERE up.user.firebaseUid = :firebaseUid")
    Optional<UserProfile> findByFirebaseUid(@Param("firebaseUid") String firebaseUid);
} 