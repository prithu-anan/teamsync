package com.teamsync.feedmanagement.mapper;

import com.teamsync.feedmanagement.dto.ReactionDetailDTO;
import com.teamsync.feedmanagement.dto.ReactionResponseDTO;
import com.teamsync.feedmanagement.entity.Reactions;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ReactionMapper {
    @Mapping(source = "user", target = "userId")
    ReactionResponseDTO reactionResponseToDTO(Reactions reaction);

    List<ReactionResponseDTO> reactionsResponseToDTO(List<Reactions> reactions);


    @Mapping(source = "user", target = "userId")
    ReactionDetailDTO reactionToDTO(Reactions reaction);

    List<ReactionDetailDTO> reactionsToDTO(List<Reactions> reactions);




}
