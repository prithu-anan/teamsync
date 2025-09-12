
package com.teamsync.message_management_service.controller;

import com.teamsync.message_management_service.dto.FileCreationDTO;
import com.teamsync.message_management_service.dto.MessageCreationDTO;
import com.teamsync.message_management_service.dto.MessageResponseDTO;
import com.teamsync.message_management_service.dto.MessageUpdateDTO;
import com.teamsync.message_management_service.dto.MessageWithUserInfoDTO;
import com.teamsync.message_management_service.dto.UserResponseDTO;
import com.teamsync.message_management_service.mapper.MessageWithUserInfoMapper;
import com.teamsync.message_management_service.client.UserClient;

import com.teamsync.message_management_service.response.SuccessResponse;
import com.teamsync.message_management_service.service.MessageService;
import com.teamsync.message_management_service.service.WebSocketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping({"/channels", "/api/channels"})
@RequiredArgsConstructor
@Slf4j
public class MessageController {

        private final MessageService messageService;
        private final WebSocketService webSocketService;
        private final MessageWithUserInfoMapper messageWithUserInfoMapper;
        private final UserClient userClient;

        @GetMapping("/{channelId}/messages")
        public ResponseEntity<SuccessResponse<List<MessageResponseDTO>>> getChannelMessages(
                        @PathVariable Long channelId) {
                List<MessageResponseDTO> messages = messageService.getChannelMessages(channelId);
                SuccessResponse<List<MessageResponseDTO>> resp = SuccessResponse.<List<MessageResponseDTO>>builder()
                                .code(HttpStatus.OK.value())
                                .status(HttpStatus.OK)
                                .message("Messages fetched successfully")
                                .data(messages)
                                .build();
                return ResponseEntity.ok(resp);
        }

        @PostMapping("/{channelId}/messages")
        public ResponseEntity<SuccessResponse<MessageResponseDTO>> createChannelMessage(
                        @PathVariable Long channelId,
                        @Valid @RequestBody MessageCreationDTO requestDto) {
                MessageResponseDTO responseDto = messageService.createChannelMessage(channelId, requestDto);
                
                // Broadcast WebSocket message asynchronously to avoid blocking the response
                CompletableFuture.runAsync(() -> {
                    try {
                        // Get sender information for WebSocket broadcast
                        UserResponseDTO sender = userClient.findById(responseDto.senderId()).getData();
                        
                        // Broadcast the new message via WebSocket with user info
                        if (requestDto.recipientId() != null) {
                                // Direct message
                                if (sender != null) {
                                        MessageWithUserInfoDTO messageWithUserInfo = messageWithUserInfoMapper.toMessageWithUserInfo(responseDto, sender);
                                        webSocketService.broadcastDirectMessageWithUserInfo(requestDto.recipientId(), messageWithUserInfo);
                                } else {
                                        webSocketService.broadcastDirectMessage(requestDto.recipientId(), responseDto);
                                }
                        } else {
                                // Channel message
                                if (sender != null) {
                                        MessageWithUserInfoDTO messageWithUserInfo = messageWithUserInfoMapper.toMessageWithUserInfo(responseDto, sender);
                                        webSocketService.broadcastNewMessageWithUserInfo(channelId, messageWithUserInfo);
                                } else {
                                        webSocketService.broadcastNewMessage(channelId, responseDto);
                                }
                        }
                    } catch (Exception e) {
                        log.error("Failed to broadcast WebSocket message for channel {}: {}", channelId, e.getMessage(), e);
                        // Fallback: try to broadcast without user info
                        try {
                            if (requestDto.recipientId() != null) {
                                webSocketService.broadcastDirectMessage(requestDto.recipientId(), responseDto);
                            } else {
                                webSocketService.broadcastNewMessage(channelId, responseDto);
                            }
                        } catch (Exception fallbackError) {
                            log.error("Fallback WebSocket broadcast also failed for channel {}: {}", channelId, fallbackError.getMessage());
                        }
                    }
                });
                
                SuccessResponse<MessageResponseDTO> resp = SuccessResponse.<MessageResponseDTO>builder()
                                .code(HttpStatus.CREATED.value())
                                .status(HttpStatus.CREATED)
                                .message("Message created successfully")
                                .data(responseDto)
                                .build();
                return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        }

// Update createMessageWithFiles method parameters
@PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<SuccessResponse<List<MessageResponseDTO>>> createMessageWithFiles(
        @RequestParam(value = "channelId", required = false) Long channelId,
        @RequestParam(value = "recipientId", required = false) Long recipientId,
        @RequestParam(value = "threadParentId", required = false) Long threadParentId,
        @RequestParam(value = "isPinned", required = false, defaultValue = "false") Boolean isPinned,  // Add this line
        @RequestParam("files") MultipartFile[] files) {

    // Create FileCreationDTO list from MultipartFile array
    List<FileCreationDTO> fileDtos = java.util.Arrays.stream(files)
            .map(file -> new FileCreationDTO(file))
            .toList();

    // Update MessageCreationDTO creation to include isPinned
    MessageCreationDTO requestDto = new MessageCreationDTO(null, channelId, recipientId, threadParentId,
            fileDtos, isPinned);  // Add isPinned parameter

    List<MessageResponseDTO> responseDtos = messageService.createMessageWithFiles(requestDto);

    // Get sender information for WebSocket broadcast
    UserResponseDTO sender = userClient.findById(responseDtos.get(0).senderId()).getData();
    
    // Broadcast the new messages via WebSocket with user info
    for (MessageResponseDTO responseDto : responseDtos) {
        if (requestDto.recipientId() != null) {
            // Direct message
            if (sender != null) {
                MessageWithUserInfoDTO messageWithUserInfo = messageWithUserInfoMapper.toMessageWithUserInfo(responseDto, sender);
                webSocketService.broadcastDirectMessageWithUserInfo(requestDto.recipientId(), messageWithUserInfo);
            } else {
                webSocketService.broadcastDirectMessage(requestDto.recipientId(), responseDto);
            }
        } else if (channelId != null) {
            // Channel message
            if (sender != null) {
                MessageWithUserInfoDTO messageWithUserInfo = messageWithUserInfoMapper.toMessageWithUserInfo(responseDto, sender);
                webSocketService.broadcastNewMessageWithUserInfo(channelId, messageWithUserInfo);
            } else {
                webSocketService.broadcastNewMessage(channelId, responseDto);
            }
        }
    }

    SuccessResponse<List<MessageResponseDTO>> resp = SuccessResponse.<List<MessageResponseDTO>>builder()
            .code(HttpStatus.CREATED.value())
            .status(HttpStatus.CREATED)
            .message("Message with files created successfully")
            .data(responseDtos)
            .build();
    return ResponseEntity.status(HttpStatus.CREATED).body(resp);
}
        @GetMapping("/{channelId}/messages/{messageId}")
        public ResponseEntity<SuccessResponse<MessageResponseDTO>> getChannelMessage(
                        @PathVariable Long channelId,
                        @PathVariable Long messageId) {
                MessageResponseDTO responseDto = messageService.getChannelMessage(channelId, messageId);
                SuccessResponse<MessageResponseDTO> resp = SuccessResponse.<MessageResponseDTO>builder()
                                .code(HttpStatus.OK.value())
                                .status(HttpStatus.OK)
                                .message("Message fetched successfully")
                                .data(responseDto)
                                .build();
                return ResponseEntity.ok(resp);
        }

        @PutMapping("/{channelId}/messages/{messageId}")
        public ResponseEntity<SuccessResponse<MessageResponseDTO>> updateChannelMessage(
                        @PathVariable Long channelId,
                        @PathVariable Long messageId,
                        @Valid @RequestBody MessageUpdateDTO requestDto) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userEmail = authentication.getName();
                MessageResponseDTO responseDto = messageService.updateChannelMessage(channelId, messageId, requestDto,
                                userEmail);
                
                // Get sender information for WebSocket broadcast
                UserResponseDTO sender = userClient.findById(responseDto.senderId()).getData();
                
                // Broadcast the message update via WebSocket with user info
                if (requestDto.recipientId() != null) {
                        // Direct message
                        if (sender != null) {
                                MessageWithUserInfoDTO messageWithUserInfo = messageWithUserInfoMapper.toMessageWithUserInfo(responseDto, sender);
                                webSocketService.broadcastDirectMessageUpdateWithUserInfo(requestDto.recipientId(), messageWithUserInfo);
                        } else {
                                webSocketService.broadcastDirectMessageUpdate(requestDto.recipientId(), responseDto);
                        }
                } else {
                        // Channel message
                        if (sender != null) {
                                MessageWithUserInfoDTO messageWithUserInfo = messageWithUserInfoMapper.toMessageWithUserInfo(responseDto, sender);
                                webSocketService.broadcastMessageUpdateWithUserInfo(channelId, messageWithUserInfo);
                        } else {
                                webSocketService.broadcastMessageUpdate(channelId, responseDto);
                        }
                }
                
                SuccessResponse<MessageResponseDTO> resp = SuccessResponse.<MessageResponseDTO>builder()
                                .code(HttpStatus.OK.value())
                                .status(HttpStatus.OK)
                                .message("Message updated successfully")
                                .data(responseDto)
                                .build();
                return ResponseEntity.ok(resp);
        }

        @DeleteMapping("/{channelId}/messages/{messageId}")
        public ResponseEntity<SuccessResponse<Void>> deleteChannelMessage(
                        @PathVariable Long channelId,
                        @PathVariable Long messageId) {
                messageService.deleteChannelMessage(channelId, messageId);
                
                // Broadcast the message deletion via WebSocket
                webSocketService.broadcastMessageDeletion(channelId, messageId);
                
                SuccessResponse<Void> resp = SuccessResponse.<Void>builder()
                                .code(HttpStatus.NO_CONTENT.value())
                                .status(HttpStatus.OK)
                                .message("Message deleted successfully")
                                .build();
                return ResponseEntity.status(HttpStatus.OK).body(resp);
        }
}