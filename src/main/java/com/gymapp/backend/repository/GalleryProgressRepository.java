package com.gymapp.backend.repository;

import com.gymapp.backend.model.User;
import com.gymapp.backend.model.GalleryProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GalleryProgressRepository extends JpaRepository<GalleryProgress, UUID> {
    List<GalleryProgress> findByUserOrderByPhotoDateDesc(User user);
    List<GalleryProgress> findByUserAndPhotoDateBetweenOrderByPhotoDateDesc(
        User user, 
        java.time.LocalDateTime startDate, 
        java.time.LocalDateTime endDate
    );
} 