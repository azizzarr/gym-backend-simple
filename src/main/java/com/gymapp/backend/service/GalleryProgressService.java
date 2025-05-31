package com.gymapp.backend.service;

import com.gymapp.backend.model.User;
import com.gymapp.backend.model.GalleryProgress;
import com.gymapp.backend.repository.UserRepository;
import com.gymapp.backend.repository.GalleryProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GalleryProgressService {
    private final GalleryProgressRepository galleryProgressRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public GalleryProgress addProgressPhoto(String firebaseUid, GalleryProgress galleryProgress, MultipartFile photoFile) {
        log.info("Adding progress photo for user with Firebase UID: {}", firebaseUid);
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User not found with Firebase UID: " + firebaseUid));
        
        // Store the file and get the path
        String photoPath = fileStorageService.storeFile(photoFile, user.getId().toString());
        
        galleryProgress.setUser(user);
        galleryProgress.setPhotoPath(photoPath);
        galleryProgress.setCreatedAt(LocalDateTime.now());
        galleryProgress.setUpdatedAt(LocalDateTime.now());
        
        return galleryProgressRepository.save(galleryProgress);
    }

    @Transactional(readOnly = true)
    public List<GalleryProgress> getUserProgressPhotos(String firebaseUid) {
        log.info("Fetching progress photos for user with Firebase UID: {}", firebaseUid);
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User not found with Firebase UID: " + firebaseUid));
        
        return galleryProgressRepository.findByUserOrderByPhotoDateDesc(user);
    }

    @Transactional(readOnly = true)
    public List<GalleryProgress> getUserProgressPhotosByDateRange(String firebaseUid, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching progress photos for user with Firebase UID: {} between {} and {}", firebaseUid, startDate, endDate);
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User not found with Firebase UID: " + firebaseUid));
        
        return galleryProgressRepository.findByUserAndPhotoDateBetweenOrderByPhotoDateDesc(user, startDate, endDate);
    }

    @Transactional
    public GalleryProgress updateProgressPhoto(UUID photoId, GalleryProgress updatedGalleryProgress, MultipartFile newPhotoFile) {
        log.info("Updating progress photo with ID: {}", photoId);
        GalleryProgress existingPhoto = galleryProgressRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Progress photo not found with ID: " + photoId));
        
        // If a new photo is provided, store it and update the path
        if (newPhotoFile != null && !newPhotoFile.isEmpty()) {
            // Delete the old file
            fileStorageService.deleteFile(existingPhoto.getPhotoPath());
            
            // Store the new file
            String newPhotoPath = fileStorageService.storeFile(newPhotoFile, existingPhoto.getUser().getId().toString());
            existingPhoto.setPhotoPath(newPhotoPath);
        }
        
        // Update other fields
        existingPhoto.setWeightKg(updatedGalleryProgress.getWeightKg());
        existingPhoto.setBmi(updatedGalleryProgress.getBmi());
        existingPhoto.setPhotoDate(updatedGalleryProgress.getPhotoDate());
        existingPhoto.setNotes(updatedGalleryProgress.getNotes());
        existingPhoto.setUpdatedAt(LocalDateTime.now());
        
        return galleryProgressRepository.save(existingPhoto);
    }

    @Transactional
    public void deleteProgressPhoto(UUID photoId) {
        log.info("Deleting progress photo with ID: {}", photoId);
        GalleryProgress photo = galleryProgressRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Progress photo not found with ID: " + photoId));
        
        // Delete the file from storage
        fileStorageService.deleteFile(photo.getPhotoPath());
        
        // Delete the database record
        galleryProgressRepository.deleteById(photoId);
    }
    
    public byte[] getPhotoBytes(String photoPath) {
        return fileStorageService.loadFileAsBytes(photoPath);
    }
} 