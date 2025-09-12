package com.teamsync.notificationservice.dto;

import lombok.Builder;

@Builder
public record NotificationCountDTO(
        Long userId,
        Long unreadCount
) {
}
