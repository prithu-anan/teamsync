package com.teamsync.feedmanagement.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.teamsync.feedmanagement.entity.FeedPosts;
import com.teamsync.feedmanagement.validation.RequiredPollOptionsForPollType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RequiredPollOptionsForPollType
public class FeedPostCreateRequest {
    @NotNull(message = "Type is required")
    private FeedPosts.FeedPostType type;
    @NotBlank(message = "Content is required")
    private String content;

    private String[] mediaUrls;

    private LocalDate eventDate;

    private String[] pollOptions;
}