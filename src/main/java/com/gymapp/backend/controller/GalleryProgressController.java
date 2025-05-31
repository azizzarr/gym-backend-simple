package com.gymapp.backend.controller;

import com.gymapp.backend.model.GalleryProgress;
import com.gymapp.backend.service.GalleryProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/gallery-progress")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://gym-app-angular.vercel.app"})
public class GalleryProgressController {
    private final GalleryProgressService galleryProgressService;

    @PostMapping(value = "/{firebaseUid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GalleryProgress> addProgressPhoto(
            @PathVariable String firebaseUid,
            @RequestPart("photo") MultipartFile photo,
            @RequestPart("data") GalleryProgress galleryProgress) {
        log.info("Received request to add progress photo for user: {}", firebaseUid);
        try {
            GalleryProgress savedPhoto = galleryProgressService.addProgressPhoto(firebaseUid, galleryProgress, photo);
            return ResponseEntity.ok(savedPhoto);
        } catch (Exception e) {
            log.error("Error adding progress photo: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{firebaseUid}")
    public ResponseEntity<List<GalleryProgress>> getUserProgressPhotos(@PathVariable String firebaseUid) {
        log.info("Received request to get progress photos for user: {}", firebaseUid);
        try {
            List<GalleryProgress> progressPhotos = galleryProgressService.getUserProgressPhotos(firebaseUid);
            return ResponseEntity.ok(progressPhotos);
        } catch (Exception e) {
            log.error("Error fetching progress photos: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{firebaseUid}/range")
    public ResponseEntity<List<GalleryProgress>> getUserProgressPhotosByDateRange(
            @PathVariable String firebaseUid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Received request to get progress photos for user: {} between {} and {}", firebaseUid, startDate, endDate);
        try {
            List<GalleryProgress> progressPhotos = galleryProgressService.getUserProgressPhotosByDateRange(firebaseUid, startDate, endDate);
            return ResponseEntity.ok(progressPhotos);
        } catch (Exception e) {
            log.error("Error fetching progress photos by date range: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping(value = "/{photoId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GalleryProgress> updateProgressPhoto(
            @PathVariable String photoId,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            @RequestPart("data") GalleryProgress updatedGalleryProgress) {
        log.info("Received request to update progress photo: {}", photoId);
        try {
            GalleryProgress updatedPhoto = galleryProgressService.updateProgressPhoto(
                    java.util.UUID.fromString(photoId),
                    updatedGalleryProgress,
                    photo
            );
            return ResponseEntity.ok(updatedPhoto);
        } catch (Exception e) {
            log.error("Error updating progress photo: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> deleteProgressPhoto(@PathVariable String photoId) {
        log.info("Received request to delete progress photo: {}", photoId);
        try {
            galleryProgressService.deleteProgressPhoto(java.util.UUID.fromString(photoId));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting progress photo: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/photo/{photoPath:.+}")
    public ResponseEntity<byte[]> getPhoto(@PathVariable String photoPath) {
        try {
            byte[] photoBytes = galleryProgressService.getPhotoBytes(photoPath);
            
            // Determine content type based on file extension
            String contentType = "image/jpeg"; // Default
            if (photoPath.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (photoPath.toLowerCase().endsWith(".gif")) {
                contentType = "image/gif";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + photoPath.substring(photoPath.lastIndexOf('/') + 1) + "\"")
                    .body(photoBytes);
        } catch (Exception e) {
            log.error("Error retrieving photo: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
} 