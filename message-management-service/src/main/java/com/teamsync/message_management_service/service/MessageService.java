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
    public void createChannelMessage(Long channelId, MessageCreationDTO requestDto) {
        // Validate channel exists
        Channels channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new NotFoundException("Channel with ID " + channelId + " not found"));
        // *************************************************************
        // Authentication authentication =
        // SecurityContextHolder.getContext().getAuthentication();
        // String email = authentication.getName();
        // ************************************************************
        String email = "a@b.com";
        UserResponseDTO sender = userClient.findByEmail(email);
        if (sender == null) {
            throw new NotFoundException("User not found with email " + email);
        }

        // Validate recipient if provided
        UserResponseDTO recipient = null;
        if (requestDto.recipientId() != null) {
            recipient = userClient.findById(requestDto.recipientId())
                    .orElseThrow(() -> new NotFoundException(
                            "Recipient with ID " + requestDto.recipientId() + " not found"));
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
                .build();

        messageRepository.save(message);
        // return messageMapper.toDto(savedMessage);
    }

    @Transactional
    public void createMessageWithFiles(MessageCreationDTO requestDto) {

        UserResponseDTO sender = userClient.getCurrentUser();
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
            recipient = userClient.findById(requestDto.recipientId())
                    .orElseThrow(() -> new NotFoundException(
                            "Recipient with ID " + requestDto.recipientId() + " not found"));
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
                    .build();

            messagesToSave.add(message);
        }

        // Save all messages in bulk
        messageRepository.saveAll(messagesToSave);
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
    public void updateChannelMessage(Long channelId, Long messageId, MessageUpdateDTO requestDto, String userEmail) {
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
            recipient = userClient.findById(requestDto.recipientId())
                    .orElseThrow(() -> new NotFoundException(
                            "Recipient with ID " + requestDto.recipientId() + " not found"));
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

        // Save updated message
        messageRepository.save(existingMessage);

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