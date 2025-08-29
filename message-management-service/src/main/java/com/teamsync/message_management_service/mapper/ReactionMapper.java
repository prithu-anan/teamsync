package com.teamsync.message_management_service.mapper;

import com.teamsync.message_management_service.dto.ReactionDetailDTO;
import com.teamsync.message_management_service.dto.ReactionResponseDTO;
import com.teamsync.message_management_service.entity.Reactions;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ReactionMapper {
    @Mapping(source = "user", target = "userId")
    ReactionResponseDTO reactionResponseToDTO(Reactions reaction);

    List<ReactionResponseDTO> reactionsResponseToDTO(List<Reactions> reactions);


    
    @Mapping(source = "user", target = "userId")
    List<ReactionDetailDTO> reactionsToDTO(List<Reactions> reactions);
}
