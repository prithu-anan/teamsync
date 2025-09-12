package com.teamsync.message_management_service.service;

import com.teamsync.message_management_service.dto.MessageResponseDTO;
import com.teamsync.message_management_service.dto.MessageWithUserInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    
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

    /**
     * Broadcast a new message to all subscribers of a channel
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void broadcastNewMessage(Long channelId, MessageResponseDTO message) {
        String destination = "/topic/channel/" + channelId;
        log.info("Broadcasting new message to channel {}: {}", channelId, message.id());
        try {
            messagingTemplate.convertAndSend(destination, message);
            log.debug("Successfully broadcasted message to channel {}: {}", channelId, message.id());
        } catch (Exception e) {
            log.error("Failed to broadcast message to channel {}: {} - {}", channelId, message.id(), e.getMessage(), e);
            throw e; // Re-throw to trigger retry
        }
    }

    /**
     * Broadcast a new message with user info to all subscribers of a channel
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void broadcastNewMessageWithUserInfo(Long channelId, MessageWithUserInfoDTO message) {
        String destination = "/topic/channel/" + channelId;
        log.info("Broadcasting new message with user info to channel {}: {}", channelId, message.id());
        try {
            messagingTemplate.convertAndSend(destination, message);
            log.debug("Successfully broadcasted message with user info to channel {}: {}", channelId, message.id());
        } catch (Exception e) {
            log.error("Failed to broadcast message with user info to channel {}: {} - {}", channelId, message.id(), e.getMessage(), e);
            throw e; // Re-throw to trigger retry
        }
    }

    /**
     * Broadcast a message update to all subscribers of a channel
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void broadcastMessageUpdate(Long channelId, MessageResponseDTO message) {
        String destination = "/topic/channel/" + channelId;
        log.info("Broadcasting message update to channel {}: {}", channelId, message.id());
        try {
            messagingTemplate.convertAndSend(destination, message);
            log.debug("Successfully broadcasted message update to channel {}: {}", channelId, message.id());
        } catch (Exception e) {
            log.error("Failed to broadcast message update to channel {}: {} - {}", channelId, message.id(), e.getMessage(), e);
            throw e; // Re-throw to trigger retry
        }
    }

    /**
     * Broadcast a message update with user info to all subscribers of a channel
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void broadcastMessageUpdateWithUserInfo(Long channelId, MessageWithUserInfoDTO message) {
        String destination = "/topic/channel/" + channelId;
        log.info("Broadcasting message update with user info to channel {}: {}", channelId, message.id());
        try {
            messagingTemplate.convertAndSend(destination, message);
            log.debug("Successfully broadcasted message update with user info to channel {}: {}", channelId, message.id());
        } catch (Exception e) {
            log.error("Failed to broadcast message update with user info to channel {}: {} - {}", channelId, message.id(), e.getMessage(), e);
            throw e; // Re-throw to trigger retry
        }
    }

    /**
     * Broadcast a message deletion to all subscribers of a channel
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void broadcastMessageDeletion(Long channelId, Long messageId) {
        String destination = "/topic/channel/" + channelId;
        log.info("Broadcasting message deletion to channel {}: {}", channelId, messageId);
        try {
            messagingTemplate.convertAndSend(destination, Map.of(
                "type", "DELETE",
                "messageId", messageId,
                "channelId", channelId
            ));
            log.debug("Successfully broadcasted message deletion to channel {}: {}", channelId, messageId);
        } catch (Exception e) {
            log.error("Failed to broadcast message deletion to channel {}: {} - {}", channelId, messageId, e.getMessage(), e);
            throw e; // Re-throw to trigger retry
        }
    }

    /**
     * Broadcast a new direct message to the recipient
     */
    public void broadcastDirectMessage(Long recipientId, MessageResponseDTO message) {
        String destination = "/topic/user/" + recipientId;
        log.info("Broadcasting direct message to user {}: {}", recipientId, message.id());
        messagingTemplate.convertAndSend(destination, message);
    }

    /**
     * Broadcast a new direct message with user info to the recipient
     */
    public void broadcastDirectMessageWithUserInfo(Long recipientId, MessageWithUserInfoDTO message) {
        String destination = "/topic/user/" + recipientId;
        log.info("Broadcasting direct message with user info to user {}: {}", recipientId, message.id());
        messagingTemplate.convertAndSend(destination, message);
    }

    /**
     * Broadcast a direct message update to the recipient
     */
    public void broadcastDirectMessageUpdate(Long recipientId, MessageResponseDTO message) {
        String destination = "/topic/user/" + recipientId;
        log.info("Broadcasting direct message update to user {}: {}", recipientId, message.id());
        messagingTemplate.convertAndSend(destination, message);
    }

    /**
     * Broadcast a direct message update with user info to the recipient
     */
    public void broadcastDirectMessageUpdateWithUserInfo(Long recipientId, MessageWithUserInfoDTO message) {
        String destination = "/topic/user/" + recipientId;
        log.info("Broadcasting direct message update with user info to user {}: {}", recipientId, message.id());
        messagingTemplate.convertAndSend(destination, message);
    }

    /**
     * Broadcast a direct message deletion to the recipient
     */
    public void broadcastDirectMessageDeletion(Long recipientId, Long messageId) {
        String destination = "/topic/user/" + recipientId;
        log.info("Broadcasting direct message deletion to user {}: {}", recipientId, messageId);
        messagingTemplate.convertAndSend(destination, Map.of(
            "type", "DELETE",
            "messageId", messageId,
            "recipientId", recipientId
        ));
    }
}
