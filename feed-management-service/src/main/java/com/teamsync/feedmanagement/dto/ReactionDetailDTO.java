package com.teamsync.feedmanagement.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReactionDetailDTO {
    private Long userId;
    private String reactionType;
    private ZonedDateTime createdAt;
}
