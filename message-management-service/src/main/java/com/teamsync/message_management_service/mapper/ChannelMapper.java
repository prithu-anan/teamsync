package com.teamsync.message_management_service.mapper;

import com.teamsync.message_management_service.dto.ChannelRequestDTO;
import com.teamsync.message_management_service.dto.ChannelResponseDTO;
import com.teamsync.message_management_service.dto.ChannelUpdateDTO;
import com.teamsync.message_management_service.entity.Channels;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ChannelMapper {
    // create new
    @Mapping(source = "projectId", target = "project")
    @Mapping(source = "memberIds", target = "members")
    Channels toEntity(ChannelRequestDTO dto);

    // read
    @Mapping(source = "project", target = "projectId")
    ChannelResponseDTO toDto(Channels entity);

    // update existing
    @Mapping(target = "id", ignore = true)                // never overwrite the PK
    void updateEntityFromDto(ChannelUpdateDTO dto, @MappingTarget Channels entity);
}
