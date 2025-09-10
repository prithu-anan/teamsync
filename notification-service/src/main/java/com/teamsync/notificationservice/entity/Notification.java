package com.teamsync.notificationservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "notifications")
public class Notification {
    
    @Id
    private String id;
    
    private Long userId;
    private String type;
    private String title;
    private String message;
    private Map<String, Object> metadata;
    
    @Builder.Default
    private Boolean isRead = false;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime readAt;
    
    public enum NotificationType {
        TASK_ASSIGNED,
        TASK_STATUS_CHANGED,
        PROJECT_MEMBER_ADDED,
        DEADLINE_REMINDER,
        TASK_COMPLETED
    }
}