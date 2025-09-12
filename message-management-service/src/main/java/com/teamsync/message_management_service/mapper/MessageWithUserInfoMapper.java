package com.teamsync.message_management_service.mapper;

import com.teamsync.message_management_service.dto.MessageResponseDTO;
import com.teamsync.message_management_service.dto.MessageWithUserInfoDTO;
import com.teamsync.message_management_service.dto.UserResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageWithUserInfoMapper {

    @Mapping(source = "message.id", target = "id")
    @Mapping(source = "message.senderId", target = "senderId")
    @Mapping(source = "sender.name", target = "senderName")
    @Mapping(source = "sender.profilePicture", target = "senderAvatar")
    @Mapping(source = "message.channelId", target = "channelId")
    @Mapping(source = "message.recipientId", target = "recipientId")
    @Mapping(source = "message.content", target = "content")
    @Mapping(source = "message.fileUrl", target = "fileUrl")
    @Mapping(source = "message.fileType", target = "fileType")
    @Mapping(source = "message.timestamp", target = "timestamp")
    @Mapping(source = "message.threadParentId", target = "threadParentId")
    @Mapping(source = "message.isPinned", target = "isPinned")
    MessageWithUserInfoDTO toMessageWithUserInfo(MessageResponseDTO message, UserResponseDTO sender);
}
