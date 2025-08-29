package com.teamsync.message_management_service.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record MessageCreationDTO(
        String content, // Optional - either content or files should be provided
        Long channelId,
        Long recipientId,
        Long threadParentId,
        List<FileCreationDTO> files // Optional - either content or files should be provided
) {
    // Custom validation to ensure either content or files is provided
    public MessageCreationDTO {
        if ((content == null || content.trim().isEmpty()) && (files == null || files.isEmpty())) {
            throw new IllegalArgumentException("Either content or files must be provided");
        }

        if (channelId == null && recipientId == null) {
            throw new IllegalArgumentException("Either channelId or recipientId must be provided");
        }
    }
}
