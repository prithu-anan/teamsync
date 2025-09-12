package com.teamsync.notificationservice.service;

import com.teamsync.notificationservice.dto.NotificationResponseDTO;
import com.teamsync.notificationservice.entity.Notification;
import com.teamsync.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final NotificationWebSocketService webSocketService;
    
    public Notification createNotification(Long userId, String type, String title, String message, Map<String, Object> metadata) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .metadata(metadata)
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // Broadcast the new notification via WebSocket
        CompletableFuture.runAsync(() -> {
            try {
                NotificationResponseDTO notificationDto = NotificationResponseDTO.fromEntity(savedNotification);
                Long unreadCount = getUnreadCount(userId);
                webSocketService.broadcastNewNotification(userId, notificationDto, unreadCount);
            } catch (Exception e) {
                log.error("Failed to broadcast new notification for user {}: {}", userId, e.getMessage(), e);
            }
        });
        
        return savedNotification;
    }
    
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }
    
    public long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }
    
    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId)
                .ifPresent(notification -> {
                    notification.setIsRead(true);
                    notification.setReadAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                });
    }
    
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unreadNotifications.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
        });
        notificationRepository.saveAll(unreadNotifications);
    }
    
    public void deleteNotification(String notificationId, Long userId) {
        notificationRepository.findById(notificationId)
                .ifPresent(notification -> {
                    if (notification.getUserId().equals(userId)) {
                        notificationRepository.delete(notification);
                    }
                });
    }
    
    public void deleteAllNotifications(Long userId) {
        notificationRepository.deleteByUserId(userId);
    }
}