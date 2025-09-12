package com.teamsync.message_management_service.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.ZonedDateTime;

/**
 * Enhanced message DTO that includes user information for WebSocket messages
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record MessageWithUserInfoDTO(
        Long id,
        Long senderId,
        String senderName,
        String senderAvatar,
        Long channelId,
        Long recipientId,
        String content,
        String fileUrl,
        String fileType,
        ZonedDateTime timestamp,
        Long threadParentId,
        Boolean isPinned
) {}
