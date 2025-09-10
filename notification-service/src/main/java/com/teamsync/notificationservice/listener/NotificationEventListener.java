package com.teamsync.notificationservice.listener;

import com.teamsync.notificationservice.event.TaskAssignedEvent;
import com.teamsync.notificationservice.event.TaskStatusChangedEvent;
import com.teamsync.notificationservice.event.ProjectMemberAddedEvent;
import com.teamsync.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {
    
    private final NotificationService notificationService;
    
    @KafkaListener(topics = "task-assigned", groupId = "notification-service")
    public void handleTaskAssigned(TaskAssignedEvent event) {
        log.info("Received TaskAssignedEvent: {}", event);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("taskId", event.getTaskId());
        metadata.put("projectId", event.getProjectId());
        metadata.put("assignedBy", event.getAssignedByUserId());
        metadata.put("deadline", event.getDeadline());
        metadata.put("priority", event.getPriority());
        
        String message = String.format("You have been assigned to task '%s' in project '%s'", 
                event.getTaskTitle(), event.getProjectTitle());
        
        notificationService.createNotification(
                event.getAssignedToUserId(),
                "TASK_ASSIGNED",
                "New Task Assigned",
                message,
                metadata
        );
    }
    
    @KafkaListener(topics = "task-status-changed", groupId = "notification-service")
    public void handleTaskStatusChanged(TaskStatusChangedEvent event) {
        log.info("Received TaskStatusChangedEvent: {}", event);
        
        if (event.getAssignedToUserId() != null && !event.getAssignedToUserId().equals(event.getChangedByUserId())) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("taskId", event.getTaskId());
            metadata.put("projectId", event.getProjectId());
            metadata.put("oldStatus", event.getOldStatus());
            metadata.put("newStatus", event.getNewStatus());
            metadata.put("changedBy", event.getChangedByUserId());
            
            String message = String.format("Task '%s' status changed from %s to %s", 
                    event.getTaskTitle(), event.getOldStatus(), event.getNewStatus());
            
            notificationService.createNotification(
                    event.getAssignedToUserId(),
                    "TASK_STATUS_CHANGED",
                    "Task Status Updated",
                    message,
                    metadata
            );
        }
    }
    
    @KafkaListener(topics = "project-member-added", groupId = "notification-service")
    public void handleProjectMemberAdded(ProjectMemberAddedEvent event) {
        log.info("Received ProjectMemberAddedEvent: {}", event);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("projectId", event.getProjectId());
        metadata.put("role", event.getRole());
        metadata.put("addedBy", event.getAddedByUserId());
        
        String message = String.format("You have been added to project '%s' with role: %s", 
                event.getProjectTitle(), event.getRole());
        
        notificationService.createNotification(
                event.getUserId(),
                "PROJECT_MEMBER_ADDED",
                "Added to Project",
                message,
                metadata
        );
    }
}