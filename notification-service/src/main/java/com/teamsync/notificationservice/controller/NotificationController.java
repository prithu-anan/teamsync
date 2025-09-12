package com.teamsync.notificationservice.controller;

import com.teamsync.notificationservice.dto.NotificationCountDTO;
import com.teamsync.notificationservice.dto.NotificationResponseDTO;
import com.teamsync.notificationservice.service.NotificationService;
import com.teamsync.notificationservice.service.NotificationWebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.teamsync.notificationservice.client.UserClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping({"/notifications", "/api/notifications"})
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationWebSocketService webSocketService;
    private final UserClient userClient;

    /**
     * Get all notifications for the current user
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getUserNotifications() {
        Long userId = getCurrentUserId();
        List<NotificationResponseDTO> notifications = notificationService.getUserNotifications(userId)
                .stream()
                .map(NotificationResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread notifications for the current user
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponseDTO>> getUnreadNotifications() {
        Long userId = getCurrentUserId();
        List<NotificationResponseDTO> notifications = notificationService.getUnreadNotifications(userId)
                .stream()
                .map(NotificationResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread notification count for the current user
     */
    @GetMapping("/count")
    public ResponseEntity<NotificationCountDTO> getUnreadCount() {
        Long userId = getCurrentUserId();
        Long unreadCount = notificationService.getUnreadCount(userId);
        NotificationCountDTO countDto = NotificationCountDTO.builder()
                .userId(userId)
                .unreadCount(unreadCount)
                .build();
        return ResponseEntity.ok(countDto);
    }

    /**
     * Mark a specific notification as read
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable String notificationId) {
        Long userId = getCurrentUserId();
        notificationService.markAsRead(notificationId);
        
        // Broadcast the read status update via WebSocket
        CompletableFuture.runAsync(() -> {
            try {
                Long unreadCount = notificationService.getUnreadCount(userId);
                webSocketService.broadcastNotificationRead(userId, notificationId, unreadCount);
            } catch (Exception e) {
                log.error("Failed to broadcast notification read status for user {}: {}", userId, e.getMessage(), e);
            }
        });
        
        return ResponseEntity.ok().build();
    }

    /**
     * Mark all notifications as read for the current user
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        Long userId = getCurrentUserId();
        notificationService.markAllAsRead(userId);
        
        // Broadcast the read status update via WebSocket
        CompletableFuture.runAsync(() -> {
            try {
                Long unreadCount = notificationService.getUnreadCount(userId);
                webSocketService.broadcastUnreadCountUpdate(userId, unreadCount);
            } catch (Exception e) {
                log.error("Failed to broadcast unread count update for user {}: {}", userId, e.getMessage(), e);
            }
        });
        
        return ResponseEntity.ok().build();
    }

    /**
     * Delete a specific notification
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable String notificationId) {
        Long userId = getCurrentUserId();
        notificationService.deleteNotification(notificationId, userId);
        
        // Broadcast the unread count update via WebSocket
        CompletableFuture.runAsync(() -> {
            try {
                Long unreadCount = notificationService.getUnreadCount(userId);
                webSocketService.broadcastUnreadCountUpdate(userId, unreadCount);
            } catch (Exception e) {
                log.error("Failed to broadcast unread count update for user {}: {}", userId, e.getMessage(), e);
            }
        });
        
        return ResponseEntity.ok().build();
    }

    /**
     * Delete all notifications for the current user
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAllNotifications() {
        Long userId = getCurrentUserId();
        notificationService.deleteAllNotifications(userId);
        
        // Broadcast the unread count update via WebSocket
        CompletableFuture.runAsync(() -> {
            try {
                webSocketService.broadcastUnreadCountUpdate(userId, 0L);
            } catch (Exception e) {
                log.error("Failed to broadcast unread count update for user {}: {}", userId, e.getMessage(), e);
            }
        });
        
        return ResponseEntity.ok().build();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            String email = null;
            if (principal instanceof String) {
                email = (String) principal; // In our JWT, we set email as the principal
            }
            if (email != null && !email.isBlank()) {
                UserClient.UserResponse resp = userClient.findByEmail(email);
                if (resp != null && resp.data != null && resp.data.id != null) {
                    return resp.data.id;
                }
            }
        }
        throw new RuntimeException("User not authenticated");
    }
}
