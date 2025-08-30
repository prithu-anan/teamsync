package com.teamsync.feedmanagement.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.teamsync.feedmanagement.entity.FeedPosts;
import lombok.Data;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FeedPostResponseDTO {
    private Long id;
    private FeedPosts.FeedPostType type;
    private Long authorId;
    private String content;
    private String[] mediaUrls;
    private ZonedDateTime createdAt;
    private LocalDate eventDate;
    private String[] pollOptions;
    private boolean isAiGenerated;
    private String aiSummary;
}