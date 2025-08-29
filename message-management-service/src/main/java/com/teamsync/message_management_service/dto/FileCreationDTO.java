package com.teamsync.message_management_service.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record FileCreationDTO(
        @NotNull(message = "File cannot be null")
        MultipartFile file
) {
    public String getFileName() {
        return file.getOriginalFilename();
    }
    
    public String getFileType() {
        return file.getContentType();
    }
} 