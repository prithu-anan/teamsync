package com.teamsync.feedmanagement.mapper;

import com.teamsync.feedmanagement.dto.EventCreationDTO;
import com.teamsync.feedmanagement.dto.EventResponseDTO;
import com.teamsync.feedmanagement.dto.EventUpdateDTO;
// import com.teamsync.feedmanagement.dto.UserUpdateDTO;
import com.teamsync.feedmanagement.entity.Events;
// import com.teamsync.feedmanagement.dto.entity.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "participants", source = "participantIds")
    @Mapping(target = "parentPost", ignore = true)
    @Mapping(target = "tentativeStartingDate", ignore = true)
    @Mapping(target = "id", ignore = true)
    Events toEntity(EventCreationDTO dto);

    @Mapping(source = "participants", target = "participantIds")
    EventResponseDTO toDto(Events entity);

    @Mapping(target = "parentPost", ignore = true)
    @Mapping(target = "tentativeStartingDate", ignore = true)
    @Mapping(target = "id", ignore = true)
    Events toEntity(EventUpdateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentPost", ignore = true)
    @Mapping(target = "tentativeStartingDate", ignore = true)
    void updateEventFromDTO(EventUpdateDTO eventUpdateDTO, @MappingTarget Events event);
}