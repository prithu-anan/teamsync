package com.teamsync.notificationservice.dto;

import com.teamsync.notificationservice.entity.Notification;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record NotificationResponseDTO(
        String id,
        Long userId,
        String type,
        String title,
        String message,
        Map<String, Object> metadata,
        Boolean isRead,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {
    public static NotificationResponseDTO fromEntity(Notification notification) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .metadata(notification.getMetadata())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
