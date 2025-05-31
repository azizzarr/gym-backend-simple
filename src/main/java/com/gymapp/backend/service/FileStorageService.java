package com.gymapp.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir:./uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("File storage directory created at: {}", this.fileStorageLocation);
        } catch (IOException ex) {
            log.error("Could not create the directory where the uploaded files will be stored", ex);
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored", ex);
        }
    }

    public String storeFile(MultipartFile file, String userId) {
        // Normalize file name
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        if (originalFileName.contains("..")) {
            throw new RuntimeException("Invalid file path sequence " + originalFileName);
        }
        
        try {
            // Check if the file's name contains invalid characters
            if (originalFileName == null || originalFileName.isEmpty()) {
                throw new RuntimeException("Invalid file name");
            }
            
            // Generate a unique file name
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + fileExtension;
            
            // Create user-specific directory if it doesn't exist
            Path userDir = this.fileStorageLocation.resolve(userId);
            Files.createDirectories(userDir);
            
            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = userDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Return the relative path to the file
            return userId + "/" + fileName;
        } catch (IOException ex) {
            log.error("Could not store file {}. Please try again!", originalFileName, ex);
            throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    public byte[] loadFileAsBytes(String filePath) {
        try {
            Path file = this.fileStorageLocation.resolve(filePath);
            return Files.readAllBytes(file);
        } catch (IOException ex) {
            log.error("Could not read file: {}", filePath, ex);
            throw new RuntimeException("Could not read file: " + filePath, ex);
        }
    }

    public void deleteFile(String filePath) {
        try {
            Path file = this.fileStorageLocation.resolve(filePath);
            Files.deleteIfExists(file);
        } catch (IOException ex) {
            log.error("Could not delete file: {}", filePath, ex);
            throw new RuntimeException("Could not delete file: " + filePath, ex);
        }
    }
} 