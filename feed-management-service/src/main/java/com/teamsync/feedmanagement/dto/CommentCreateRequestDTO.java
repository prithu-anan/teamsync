package com.teamsync.feedmanagement.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CommentCreateRequestDTO {
    @NotNull(message = "Post ID is required")
    private Long postId;

    @NotBlank(message = "Content is required")
    private String content;

    private Long parentCommentId;
}