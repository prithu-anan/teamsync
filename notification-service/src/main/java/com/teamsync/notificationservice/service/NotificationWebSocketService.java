package com.teamsync.notificationservice.service;

import com.teamsync.notificationservice.dto.NotificationResponseDTO;
import com.teamsync.notificationservice.dto.NotificationWebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Broadcast a new notification to a specific user
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void broadcastNewNotification(Long userId, NotificationResponseDTO notification, Long unreadCount) {
        String destination = "/topic/user/" + userId + "/notifications";
        log.info("Broadcasting new notification to user {}: {}", userId, notification.id());
        try {
            NotificationWebSocketMessage message = NotificationWebSocketMessage.newNotification(notification, unreadCount);
            messagingTemplate.convertAndSend(destination, message);
            log.debug("Successfully broadcasted new notification to user {}: {}", userId, notification.id());
        } catch (Exception e) {
            log.error("Failed to broadcast new notification to user {}: {} - {}", userId, notification.id(), e.getMessage(), e);
            throw e; // Re-throw to trigger retry
        }
    }

    /**
     * Broadcast notification read status update to a specific user
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void broadcastNotificationRead(Long userId, String notificationId, Long unreadCount) {
        String destination = "/topic/user/" + userId + "/notifications";
        log.info("Broadcasting notification read status to user {}: {}", userId, notificationId);
        try {
            NotificationWebSocketMessage message = NotificationWebSocketMessage.notificationRead(notificationId, userId, unreadCount);
            messagingTemplate.convertAndSend(destination, message);
            log.debug("Successfully broadcasted notification read status to user {}: {}", userId, notificationId);
        } catch (Exception e) {
            log.error("Failed to broadcast notification read status to user {}: {} - {}", userId, notificationId, e.getMessage(), e);
            throw e; // Re-throw to trigger retry
        }
    }

    /**
     * Broadcast unread count update to a specific user
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void broadcastUnreadCountUpdate(Long userId, Long unreadCount) {
        String destination = "/topic/user/" + userId + "/notifications";
        log.info("Broadcasting unread count update to user {}: {}", userId, unreadCount);
        try {
            NotificationWebSocketMessage message = NotificationWebSocketMessage.countUpdate(userId, unreadCount);
            messagingTemplate.convertAndSend(destination, message);
            log.debug("Successfully broadcasted unread count update to user {}: {}", userId, unreadCount);
        } catch (Exception e) {
            log.error("Failed to broadcast unread count update to user {}: {} - {}", userId, unreadCount, e.getMessage(), e);
            throw e; // Re-throw to trigger retry
        }
    }

    /**
     * Fallback method to broadcast message without retry (for critical failures)
     */
    private void broadcastWithFallback(String destination, Object message, String operation) {
        try {
            messagingTemplate.convertAndSend(destination, message);
            log.debug("Fallback broadcast successful for {}: {}", operation, destination);
        } catch (Exception e) {
            log.error("Fallback broadcast also failed for {}: {} - {}", operation, destination, e.getMessage());
            // Could implement additional fallback like storing in database for later retry
        }
    }
}
