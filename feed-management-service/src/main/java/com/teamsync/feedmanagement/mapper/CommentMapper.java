package com.teamsync.feedmanagement.mapper;

import com.teamsync.feedmanagement.dto.CommentCreateRequestDTO;
import com.teamsync.feedmanagement.dto.CommentResponseDTO;
import com.teamsync.feedmanagement.dto.CommentUpdateRequestDTO;
import com.teamsync.feedmanagement.dto.ReplyCreateRequestDTO;
import com.teamsync.feedmanagement.entity.Comments;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "post.id", target = "postId")
    @Mapping(source = "author", target = "authorId")
    @Mapping(source = "parentComment.id", target = "parentCommentId")
    @Mapping(target = "reactions", ignore = true) // Handle reactions separately
    CommentResponseDTO toResponseDTO(Comments comment);

    List<CommentResponseDTO> toResponseDTOList(List<Comments> comments);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "parentComment", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "replyCount", ignore = true)
    Comments toEntity(CommentCreateRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "parentComment", ignore = true)
    Comments toEntity(CommentUpdateRequestDTO dto);

    // NEW MAPPING FOR REPLY DTO TO ENTITY
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "post", ignore = true)
    // @Mapping(target = "author", ignore = true)
    @Mapping(target = "parentComment", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "replyCount", ignore = true)
    @Mapping(source = "author_id", target = "author")
    Comments toEntity(ReplyCreateRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "parentComment", ignore = true)
    void updateEntityFromDTO(CommentUpdateRequestDTO dto, @MappingTarget Comments comment);
}