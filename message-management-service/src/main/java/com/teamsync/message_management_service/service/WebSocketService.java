package com.teamsync.message_management_service.service;

import com.teamsync.message_management_service.dto.MessageResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcast a new message to all subscribers of a channel
     */
    public void broadcastNewMessage(Long channelId, MessageResponseDTO message) {
        String destination = "/topic/channel/" + channelId;
        log.info("Broadcasting new message to channel {}: {}", channelId, message.id());
        messagingTemplate.convertAndSend(destination, message);
    }

    /**
     * Broadcast a message update to all subscribers of a channel
     */
    public void broadcastMessageUpdate(Long channelId, MessageResponseDTO message) {
        String destination = "/topic/channel/" + channelId;
        log.info("Broadcasting message update to channel {}: {}", channelId, message.id());
        messagingTemplate.convertAndSend(destination, message);
    }

    /**
     * Broadcast a message deletion to all subscribers of a channel
     */
    public void broadcastMessageDeletion(Long channelId, Long messageId) {
        String destination = "/topic/channel/" + channelId;
        log.info("Broadcasting message deletion to channel {}: {}", channelId, messageId);
        messagingTemplate.convertAndSend(destination, Map.of(
            "type", "DELETE",
            "messageId", messageId,
            "channelId", channelId
        ));
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
     * Broadcast a direct message update to the recipient
     */
    public void broadcastDirectMessageUpdate(Long recipientId, MessageResponseDTO message) {
        String destination = "/topic/user/" + recipientId;
        log.info("Broadcasting direct message update to user {}: {}", recipientId, message.id());
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
