package com.teamsync.feedmanagement.mapper;

import com.teamsync.feedmanagement.dto.PollVoteResponseDTO;
import com.teamsync.feedmanagement.entity.PollVotes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PollVoteMapper {

    @Mapping(source = "poll.id", target = "pollId")
    @Mapping(source = "user", target = "userId")
    @Mapping(source = "selectedOption", target = "selectedOption")
    PollVoteResponseDTO toDTO(PollVotes pollVote);
}