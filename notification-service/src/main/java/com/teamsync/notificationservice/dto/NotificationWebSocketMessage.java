package com.teamsync.notificationservice.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record NotificationWebSocketMessage(
        String type, // "NEW_NOTIFICATION", "NOTIFICATION_READ", "NOTIFICATION_COUNT_UPDATE"
        String notificationId,
        Long userId,
        String notificationType,
        String title,
        String message,
        Map<String, Object> metadata,
        Boolean isRead,
        LocalDateTime createdAt,
        LocalDateTime readAt,
        Long unreadCount
) {
    public static NotificationWebSocketMessage newNotification(NotificationResponseDTO notification, Long unreadCount) {
        return NotificationWebSocketMessage.builder()
                .type("NEW_NOTIFICATION")
                .notificationId(notification.id())
                .userId(notification.userId())
                .notificationType(notification.type())
                .title(notification.title())
                .message(notification.message())
                .metadata(notification.metadata())
                .isRead(notification.isRead())
                .createdAt(notification.createdAt())
                .readAt(notification.readAt())
                .unreadCount(unreadCount)
                .build();
    }

    public static NotificationWebSocketMessage notificationRead(String notificationId, Long userId, Long unreadCount) {
        return NotificationWebSocketMessage.builder()
                .type("NOTIFICATION_READ")
                .notificationId(notificationId)
                .userId(userId)
                .unreadCount(unreadCount)
                .build();
    }

    public static NotificationWebSocketMessage countUpdate(Long userId, Long unreadCount) {
        return NotificationWebSocketMessage.builder()
                .type("NOTIFICATION_COUNT_UPDATE")
                .userId(userId)
                .unreadCount(unreadCount)
                .build();
    }
}
