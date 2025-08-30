package com.teamsync.feedmanagement.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
// import edu.teamsync.teamsync.dto.reactionsDTO.ReactionDetailDTO;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CommentUpdateRequestDTO {
    @NotNull(message = "Post ID is required")
    private Long postId;

    @NotNull(message = "Author ID is required")
    private Long authorId;

    @NotBlank(message = "Content is required")
    private String content;

    private ZonedDateTime timestamp;
    private Long parentCommentId;
    private List<ReactionDetailDTO> reactions;
    private Integer replyCount;
}