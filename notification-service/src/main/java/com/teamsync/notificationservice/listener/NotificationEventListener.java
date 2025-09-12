package com.teamsync.notificationservice.listener;

import com.teamsync.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
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
    public void handleTaskAssigned(GenericRecord event) {
        log.info("Received TaskAssignedEvent: {}", event);
        
        try {
            Long assignedToUserId = (Long) event.get("assignedToUserId");
            String taskTitle = event.get("taskTitle").toString();
            String projectTitle = event.get("projectTitle").toString();
            Long taskId = (Long) event.get("taskId");
            Long projectId = (Long) event.get("projectId");
            Long assignedByUserId = (Long) event.get("assignedByUserId");
            Object deadline = event.get("deadline");
            Object priority = event.get("priority");
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("taskId", taskId);
            metadata.put("projectId", projectId);
            metadata.put("assignedBy", assignedByUserId);
            if (deadline != null) metadata.put("deadline", deadline.toString());
            if (priority != null) metadata.put("priority", priority.toString());
            
            String message = String.format("You have been assigned to task '%s' in project '%s'", 
                    taskTitle, projectTitle);
            
            notificationService.createNotification(
                    assignedToUserId,
                    "TASK_ASSIGNED",
                    "New Task Assigned",
                    message,
                    metadata
            );
        } catch (Exception e) {
            log.error("Error processing TaskAssignedEvent: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "task-status-changed", groupId = "notification-service")
    public void handleTaskStatusChanged(GenericRecord event) {
        log.info("Received TaskStatusChangedEvent: {}", event);
        
        try {
            Long assignedToUserId = (Long) event.get("assignedToUserId");
            Long changedByUserId = (Long) event.get("changedByUserId");
            
            if (assignedToUserId != null && !assignedToUserId.equals(changedByUserId)) {
                String taskTitle = event.get("taskTitle").toString();
                String oldStatus = event.get("oldStatus").toString();
                String newStatus = event.get("newStatus").toString();
                Long taskId = (Long) event.get("taskId");
                Long projectId = (Long) event.get("projectId");
                
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("taskId", taskId);
                metadata.put("projectId", projectId);
                metadata.put("oldStatus", oldStatus);
                metadata.put("newStatus", newStatus);
                metadata.put("changedBy", changedByUserId);
                
                String message = String.format("Task '%s' status changed from %s to %s", 
                        taskTitle, oldStatus, newStatus);
                
                notificationService.createNotification(
                        assignedToUserId,
                        "TASK_STATUS_CHANGED",
                        "Task Status Updated",
                        message,
                        metadata
                );
            }
        } catch (Exception e) {
            log.error("Error processing TaskStatusChangedEvent: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "project-member-added", groupId = "notification-service")
    public void handleProjectMemberAdded(GenericRecord event) {
        log.info("Received ProjectMemberAddedEvent: {}", event);
        
        try {
            Long userId = (Long) event.get("userId");
            String projectTitle = event.get("projectTitle").toString();
            String role = event.get("role").toString();
            Long projectId = (Long) event.get("projectId");
            Long addedByUserId = (Long) event.get("addedByUserId");
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("projectId", projectId);
            metadata.put("role", role);
            metadata.put("addedBy", addedByUserId);
            
            String message = String.format("You have been added to project '%s' with role: %s", 
                    projectTitle, role);
            
            notificationService.createNotification(
                    userId,
                    "PROJECT_MEMBER_ADDED",
                    "Added to Project",
                    message,
                    metadata
            );
        } catch (Exception e) {
            log.error("Error processing ProjectMemberAddedEvent: {}", e.getMessage(), e);
        }
    }
}