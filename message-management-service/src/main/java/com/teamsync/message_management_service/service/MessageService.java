package com.teamsync.message_management_service.service;

import com.teamsync.message_management_service.dto.FileCreationDTO;
import com.teamsync.message_management_service.dto.MessageCreationDTO;
import com.teamsync.message_management_service.dto.MessageResponseDTO;
import com.teamsync.message_management_service.dto.MessageUpdateDTO;
import com.teamsync.message_management_service.entity.Channels;
import com.teamsync.message_management_service.entity.Messages;
// import com.teamsync.message_management_service.entity.Users;
import com.teamsync.message_management_service.dto.UserResponseDTO;

import com.teamsync.message_management_service.exception.NotFoundException;
import com.teamsync.message_management_service.mapper.MessageMapper;
import com.teamsync.message_management_service.repository.ChannelRepository;
import com.teamsync.message_management_service.repository.MessageRepository;
// import com.teamsync.message_management_service.repository.UserRepository;
import com.teamsync.message_management_service.client.UserClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChannelRepository channelRepository;

    // @Autowired
    // private UserRepository userRepository;

    @Autowired
    private UserClient userClient;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private AzureStorageService azureStorageService;

    // @Autowired
    // private UserService userService;

    public List<MessageResponseDTO> getChannelMessages(Long channelId) {
        // Validate channel exists
        if (!channelRepository.existsById(channelId)) {
            throw new NotFoundException("Channel with ID " + channelId + " not found");
        }

        List<Messages> messages = messageRepository.findByChannelIdOrderByTimestampAsc(channelId);
        return messages.stream()
                .map(messageMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageResponseDTO createChannelMessage(Long channelId, MessageCreationDTO requestDto) {
        // Validate channel exists
        Channels channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new NotFoundException("Channel with ID " + channelId + " not found"));
        // *************************************************************
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        // ************************************************************
UserResponseDTO sender = userClient.findByEmail(email).getData(); // ✅ unwrap
        if (sender == null) {
            throw new NotFoundException("User not found with email " + email);
        }

        // Validate recipient if provided
        UserResponseDTO recipient = null;
        if (requestDto.recipientId() != null) {    recipient = userClient.findById(requestDto.recipientId()).getData(); // ✅ unwrap
    if (recipient == null) {
        throw new NotFoundException("Recipient with ID " + requestDto.recipientId() + " not found");
    }
        }

        // Validate thread parent if provided
        Messages threadParent = null;
        if (requestDto.threadParentId() != null) {
            threadParent = messageRepository.findById(requestDto.threadParentId())
                    .orElseThrow(() -> new NotFoundException(
                            "Thread parent message with ID " + requestDto.threadParentId() + " not found"));
        }

        Messages message = Messages.builder()
                .content(requestDto.content())
                .sender(sender.getId())
                .channel(channel)
                .recipient(recipient.getId())
                .threadParent(threadParent)
                .timestamp(ZonedDateTime.now())
                .isPinned(requestDto.isPinned() != null ? requestDto.isPinned() : false) // Add this line
                .build();

        Messages savedMessage = messageRepository.save(message);
        return messageMapper.toDto(savedMessage);
    }

    @Transactional
    public List<MessageResponseDTO> createMessageWithFiles(MessageCreationDTO requestDto) {

UserResponseDTO sender = userClient.getCurrentUser().getData(); // ✅ unwrap
        if (sender == null) {
            throw new NotFoundException("User is unauthorized");
        }

        // validate channel if provided

        Channels channel = null;

        if (requestDto.channelId() != null) {
            channel = channelRepository.findById(requestDto.channelId())
                    .orElseThrow(
                            () -> new NotFoundException("Channel with ID " + requestDto.channelId() + " not found"));
        }

        // Validate recipient if provided
        UserResponseDTO recipient = null;
        if (requestDto.recipientId() != null) {

                recipient = userClient.findById(requestDto.recipientId()).getData(); // ✅ unwrap
    if (recipient == null) {
        throw new NotFoundException("Recipient with ID " + requestDto.recipientId() + " not found");
    }
        }

        // Validate thread parent if provided
        Messages threadParent = null;
        if (requestDto.threadParentId() != null) {
            threadParent = messageRepository.findById(requestDto.threadParentId())
                    .orElseThrow(() -> new NotFoundException(
                            "Thread parent message with ID " + requestDto.threadParentId() + " not found"));
        }

        // Create list of messages to save
        List<Messages> messagesToSave = new ArrayList<>();

        // Create a separate message for each file
        for (FileCreationDTO fileDto : requestDto.files()) {
            String fileUrl = azureStorageService.uploadFile(fileDto.file());
            String fileType = fileDto.getFileType();

            String originalFileName = fileDto.file().getOriginalFilename();

            Messages message = Messages.builder()
                    .content(originalFileName)
                    .sender(sender.getId())
                    .channel(channel)
                    .recipient(recipient.getId())
                    .threadParent(threadParent)
                    .timestamp(ZonedDateTime.now())
                    .fileUrl(fileUrl)
                    .fileType(fileType)
                    .isPinned(requestDto.isPinned() != null ? requestDto.isPinned() : false) // Add this line
                    .build();

            messagesToSave.add(message);
        }

        // Save all messages in bulk
        List<Messages> savedMessages = messageRepository.saveAll(messagesToSave);

        // Convert to DTOs and return
        return savedMessages.stream()
                .map(messageMapper::toDto)
                .collect(Collectors.toList());
    }

    public MessageResponseDTO getChannelMessage(Long channelId, Long messageId) {
        // Validate channel exists
        if (!channelRepository.existsById(channelId)) {
            throw new NotFoundException("Channel with ID " + channelId + " not found");
        }

        // Find message by ID and channel ID
        Messages message = messageRepository.findByIdAndChannelId(messageId, channelId)
                .orElseThrow(() -> new NotFoundException(
                        "Message with ID " + messageId + " not found in channel " + channelId));

        return messageMapper.toDto(message);
    }

    @Transactional
    public MessageResponseDTO updateChannelMessage(Long channelId, Long messageId, MessageUpdateDTO requestDto,
            String userEmail) {
        // Validate channel exists
        if (!channelRepository.existsById(channelId)) {
            throw new NotFoundException("Channel with ID " + channelId + " not found");
        }

        // Validate message exists in the channel
        Messages existingMessage = messageRepository.findByIdAndChannelId(messageId, channelId)
                .orElseThrow(() -> new NotFoundException(
                        "Message with ID " + messageId + " not found in channel " + channelId));

        // Validate channel in request body matches path channel
        Channels channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new NotFoundException("Channel with ID " + channelId + " not found"));

        // Validate recipient if provided
        UserResponseDTO recipient = null;
        if (requestDto.recipientId() != null) {


                recipient = userClient.findById(requestDto.recipientId()).getData(); // ✅ unwrap
    if (recipient == null) {
        throw new NotFoundException("Recipient with ID " + requestDto.recipientId() + " not found");
    }
        }

        // // Validate thread parent if provided
        // Messages threadParent = null;
        // if (requestDto.threadParentId() != null) {
        // threadParent = messageRepository.findById(requestDto.threadParentId())
        // .orElseThrow(() -> new NotFoundException("Thread parent message with ID " +
        // requestDto.threadParentId() + " not found"));
        // }

        // existingMessage.setSender(sender);
        existingMessage.setChannel(channel);
        existingMessage.setRecipient(recipient.getId());
        existingMessage.setContent(requestDto.content());
        // message.setThreadParent(threadParent);
        // Add this line for isPinned update
        if (requestDto.isPinned() != null) {
            existingMessage.setIsPinned(requestDto.isPinned());
        }
        // Save updated message
        Messages updatedMessage = messageRepository.save(existingMessage);
        return messageMapper.toDto(updatedMessage);
    }

    @Transactional
    public void deleteChannelMessage(Long channelId, Long messageId) {
        // Validate channel exists
        if (!channelRepository.existsById(channelId)) {
            throw new NotFoundException("Channel with ID " + channelId + " not found");
        }

        // Validate message exists in the channel
        Messages message = messageRepository.findByIdAndChannelId(messageId, channelId)
                .orElseThrow(() -> new NotFoundException(
                        "Message with ID " + messageId + " not found in channel " + channelId));

        // Delete associated file from Azure Blob Storage if fileUrl exists
        if (message.getFileUrl() != null && !message.getFileUrl().trim().isEmpty()) {
            try {
                int deletedFilesCount = azureStorageService.deleteFilesByUrls(new String[] { message.getFileUrl() });
                logger.info("Deleted {} files from Azure Blob Storage for message: {} in channel: {}",
                        deletedFilesCount, messageId, channelId);
            } catch (Exception e) {
                // Log the error but continue with deletion to avoid blocking the operation
                logger.error("Failed to delete file from Azure Blob Storage for message: {} in channel: {} - {}",
                        messageId, channelId, e.getMessage(), e);
            }
        }

        messageRepository.delete(message);
    }
}