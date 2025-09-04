package com.teamsync.message_management_service.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.ZonedDateTime;


// MessageResponseDTO - Add isPinned field
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record MessageResponseDTO(
        Long id,
        Long senderId,
        Long channelId,
        Long recipientId,
        String content,
        String fileUrl,
        String fileType,
        ZonedDateTime timestamp,
        Long threadParentId,
        Boolean isPinned  // Add this field
) {}