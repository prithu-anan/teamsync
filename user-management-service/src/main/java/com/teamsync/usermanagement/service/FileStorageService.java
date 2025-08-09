package com.teamsync.usermanagement.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Service
public class FileStorageService {

    public byte[] storeFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
        
        // Validate file size (10MB max)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 10MB");
        }
        
        return file.getBytes();
    }

    public String generateProfilePictureUrl(Long userId) {
        return "data:image/jpeg;base64"; // We'll return base64 data
    }
}