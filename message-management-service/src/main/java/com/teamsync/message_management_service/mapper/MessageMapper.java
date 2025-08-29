package com.teamsync.message_management_service.mapper;
import com.teamsync.message_management_service.dto.MessageCreationDTO;
import com.teamsync.message_management_service.dto.MessageResponseDTO;
import com.teamsync.message_management_service.dto.MessageUpdateDTO;
import com.teamsync.message_management_service.entity.Messages;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "channel", ignore = true)
    @Mapping(target = "recipient", ignore = true)
    @Mapping(target = "threadParent", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    Messages toEntity(MessageCreationDTO dto);

    @Mapping(source = "sender", target = "senderId")
    @Mapping(source = "channel.id", target = "channelId")
    @Mapping(source = "recipient", target = "recipientId")
    @Mapping(source = "threadParent.id", target = "threadParentId")
//    @Mapping(target = "sentimentScore", constant = "0.0")
//    @Mapping(target = "suggestedReplies", expression = "java(java.util.Arrays.asList())")
    MessageResponseDTO toDto(Messages entity);

    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "channel", ignore = true)
    @Mapping(target = "recipient", ignore = true)
    @Mapping(target = "threadParent", ignore = true)
    @Mapping(target = "id", ignore = true)
    Messages toEntity(MessageUpdateDTO dto);
}
