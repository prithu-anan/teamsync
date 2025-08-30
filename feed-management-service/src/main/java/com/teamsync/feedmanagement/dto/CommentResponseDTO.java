package com.teamsync.feedmanagement.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
// import edu.teamsync.teamsync.dto.reactionsDTO.ReactionDetailDTO;
import lombok.Data;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CommentResponseDTO {
    private Long id;
    private Long postId;
    private Long authorId;
    private String content;
    private ZonedDateTime timestamp;
    private Long parentCommentId;
    private List<ReactionDetailDTO> reactions;
    private Integer replyCount;
}