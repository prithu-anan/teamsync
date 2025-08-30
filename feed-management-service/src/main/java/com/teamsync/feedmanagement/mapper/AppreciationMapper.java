package com.teamsync.feedmanagement.mapper;

import com.teamsync.feedmanagement.dto.AppreciationCreateDTO;
import com.teamsync.feedmanagement.dto.AppreciationResponseDTO;
import com.teamsync.feedmanagement.dto.AppreciationUpdateDTO;
import com.teamsync.feedmanagement.entity.Appreciations;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AppreciationMapper {
    AppreciationMapper INSTANCE = Mappers.getMapper(AppreciationMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentPost", ignore = true)
    @Mapping(target = "fromUser", ignore = true)
    @Mapping(target = "toUser", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(source = "message", target = "message")
    Appreciations toEntity(AppreciationCreateDTO dto);

    @Mapping(source = "parentPost.id", target = "parentPostId")
    @Mapping(source = "fromUser", target = "fromUserId")
    @Mapping(source = "fromUser", target = "fromUserName")
    @Mapping(source = "toUser", target = "toUserId")
    @Mapping(source = "toUser", target = "toUserName")
    AppreciationResponseDTO toResponseDTO(Appreciations entity);

    @Mapping(target = "parentPost", ignore = true)
    @Mapping(target = "fromUser", ignore = true)
    @Mapping(target = "toUser", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDTO(AppreciationUpdateDTO dto, @MappingTarget Appreciations entity);
}